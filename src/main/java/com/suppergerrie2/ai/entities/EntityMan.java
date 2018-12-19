package com.suppergerrie2.ai.entities;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.suppergerrie2.ai.MinecraftAI;
import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.inventory.ItemHandlerMan;
import com.suppergerrie2.ai.items.DebugItem;
import com.suppergerrie2.ai.networking.PacketHandler;
import com.suppergerrie2.ai.networking.SyncHandsMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class EntityMan extends EntityLiving implements IEntityAdditionalSpawnData {

    public boolean playerTexturesLoaded = false;
    public Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
    public String skinType;

    private FakePlayer fakePlayer;

    private int miningTicks = 0;
    private BlockPos lastMinePos = BlockPos.ORIGIN.down();
    private float blockSoundTimer;
    private boolean lastTickLeftClicked = false;

    private final ItemHandlerMan itemHandler;
    private GameProfile profile;

    public boolean leftClicking;
    public boolean rightClicking;

    private int selectedItemIndex = 0;

    @SuppressWarnings("unused") //This constructor is needed for forge to work
    public EntityMan(World worldIn) {
        this(worldIn, "BOT");
    }

    public EntityMan(World worldIn, String name) {
        super(worldIn);

        this.setCustomNameTag(name);
        setAlwaysRenderNameTag(true);

        profile = new GameProfile(null, name);

        this.setAIMoveSpeed(0.3f);

        itemHandler = new ItemHandlerMan();

    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.LUCK);
        this.getAttributeMap().registerAttribute(EntityPlayer.REACH_DISTANCE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();


        if (fakePlayer == null && !world.isRemote) {
            fakePlayer = new FakePlayer((WorldServer) this.world, profile, this);
        }

        //Updates Animations - By Mechanist
        updateAction();

        if (this.isDead) {
            this.resetMining();
            return;
        }

        if (!world.isRemote) {
            this.setHeldItem(EnumHand.MAIN_HAND, this.itemHandler.getStackInSlot(selectedItemIndex));

            fakePlayer.setPosition(posX, posY, posZ);
            fakePlayer.onUpdate();

            RayTraceResult result = this.rayTraceBlockEntity();

            if (leftClicking) {

                leftClick(result);
            } else {
                lastTickLeftClicked = false;
                if (this.lastMinePos.getY() > 0) {
                    resetMining();
                }
            }

            if (rightClicking) {
                rightClick(result);
            }

            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(1.0D, 0.0D, 1.0D));

            for (EntityItem item : items) {
                pickup(item);
            }
        }
    }

    //Adds swinging animation - By Mechanist
    private void updateAction() {
        super.updateEntityActionState();
        this.updateArmSwingProgress();
        this.rotationYawHead = this.rotationYaw;
    }

    //TODO: Check if this works with different kind of items. But I'm going to make a gui for that first
    private void pickup(EntityItem item) {
        if (item.cannotPickup()) return;

        ItemStack stack = item.getItem();

        for (int i = 0; i < this.itemHandler.getSlots() && !stack.isEmpty(); i++) {
            stack = this.itemHandler.insertItem(i, stack, false);

            PacketHandler.INSTANCE.sendToAllTracking(new SyncHandsMessage(this.itemHandler.getStackInSlot(i), getEntityId(), i, selectedItemIndex), this);
        }

        this.setHeldItem(EnumHand.MAIN_HAND, this.itemHandler.getStackInSlot(this.selectedItemIndex));

        if (stack.isEmpty()) {
            item.setDead();
        }
    }

    @Override
    public void setHeldItem(EnumHand hand, @Nonnull ItemStack stack) {
        if (world.isRemote) {
            System.out.println(stack);
        }

        if (stack != this.getHeldItem(hand)) {
            super.setHeldItem(hand, stack);
            fakePlayer.setHeldItem(hand, stack);

            PacketHandler.INSTANCE.sendToAllTracking(new SyncHandsMessage(stack, getEntityId(), selectedItemIndex, selectedItemIndex), this);
        }
    }

    @Override
    @Nonnull
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        switch (slotIn) {
            case MAINHAND:
                return this.itemHandler.getStackInSlot(selectedItemIndex);
            case OFFHAND:
                return this.itemHandler.getOffhand();
            case FEET:
            case LEGS:
            case CHEST:
            case HEAD:
                return super.getItemStackFromSlot(slotIn);
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!(stack.getItem() instanceof DebugItem) && hand == EnumHand.MAIN_HAND) {
            player.openGui(MinecraftAI.instance, Reference.DEBUG_INV_ID, world, this.getEntityId(), 0, 0);
        }

        return super.processInteract(player, hand);
    }

    private void leftClick(RayTraceResult result) {

        if (result == null) return;

        switch (result.typeOfHit) {
            case BLOCK:
                mine(result.getBlockPos());
                swingArm(EnumHand.MAIN_HAND);
                break;
            case ENTITY:
                if (!lastTickLeftClicked) {
                    fakePlayer.attackTargetEntityWithCurrentItem(result.entityHit);
                    swingArm(EnumHand.MAIN_HAND);
                }
            case MISS:
            default:
                resetMining();
                break;
        }
        lastTickLeftClicked = true;
    }

    private void rightClick(RayTraceResult result) {
        for (EnumHand hand : EnumHand.values()) {

            switch (result.typeOfHit) {
                case BLOCK:
                    BlockPos blockpos = result.getBlockPos();

                    if (this.world.getBlockState(blockpos).getMaterial() != Material.AIR) {

                        EnumActionResult enumactionresult = rightClickBlock(blockpos, result.sideHit, result.hitVec, hand);

                        if (enumactionresult == EnumActionResult.SUCCESS) {
                            this.swingArm(hand);

                            return;
                        }
                    }

                    break;

                default:
                    break;
            }

            ItemStack itemstack = getHeldItem(hand);

            if (!itemstack.isEmpty() && itemRightClick(hand) == EnumActionResult.SUCCESS) {
//                this.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                return;
            }
        }
    }

    private EnumActionResult itemRightClick(EnumHand hand) {
        ItemStack itemstack = getHeldItem(hand);

        if (fakePlayer.getCooldownTracker().hasCooldown(itemstack.getItem())) {
            return EnumActionResult.PASS;
        } else {
            int i = itemstack.getCount();
            ActionResult<ItemStack> actionresult = itemstack.useItemRightClick(world, fakePlayer, hand);
            ItemStack itemstack1 = actionresult.getResult();

            if (itemstack1 != itemstack || itemstack1.getCount() != i) {
                this.setHeldItem(hand, itemstack1);
                if (itemstack1.isEmpty()) {
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(fakePlayer, itemstack, hand);
                }
            }

            return actionresult.getType();
        }
    }

    private EnumActionResult rightClickBlock(BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {
        ItemStack itemstack = getHeldItem(hand);
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());
        boolean flag = false;

        if (!world.getWorldBorder().contains(pos)) {
            return EnumActionResult.FAIL;
        } else {

            EnumActionResult ret = itemstack.onItemUseFirst(fakePlayer, world, pos, hand, direction, f, f1, f2);
            if (ret != EnumActionResult.PASS) {
                return ret;
            }

            IBlockState iblockstate = world.getBlockState(pos);
            boolean bypass = getHeldItemMainhand().doesSneakBypassUse(world, pos, fakePlayer) && getHeldItemOffhand().doesSneakBypassUse(world, pos, fakePlayer);

            if ((!this.isSneaking() || bypass)) {
                flag = iblockstate.getBlock().onBlockActivated(world, pos, iblockstate, fakePlayer, hand, direction, f, f1, f2);
            }

            if (!flag && itemstack.getItem() instanceof ItemBlock) {
                ItemBlock itemblock = (ItemBlock) itemstack.getItem();

                if (!itemblock.canPlaceBlockOnSide(world, pos, direction, fakePlayer, itemstack)) {
                    return EnumActionResult.FAIL;
                }
            }

            if (!flag) {
                if (itemstack.isEmpty()) {
                    return EnumActionResult.PASS;
                } else if (fakePlayer.getCooldownTracker().hasCooldown(itemstack.getItem())) {
                    return EnumActionResult.PASS;
                } else {
                    return itemstack.onItemUse(fakePlayer, world, pos, hand, direction, f, f1, f2);
                }
            } else {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    //TODO: Sounds
    private void mine(BlockPos pos) {
        if (!this.world.getWorldBorder().contains(pos) || pos.distanceSq(getPosition()) > this.getBlockReachDistance() * this.getBlockReachDistance()) {
            resetMining();
            return;
        }

        if (!lastMinePos.equals(pos)) {
            resetMining();
        }

        lastMinePos = pos;

        miningTicks++;

        IBlockState state = world.getBlockState(pos);
        if (this.blockSoundTimer % 4.0F == 0.0F) {
            SoundType soundtype = state.getBlock().getSoundType(state, world, pos, this);
            this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0f) / 8.0f, soundtype.getPitch() * 0.5f, false);
        }

        ++this.blockSoundTimer;

        this.world.sendBlockBreakProgress(this.getEntityId(), pos, (int) (state.getPlayerRelativeBlockHardness(fakePlayer, world, pos) * miningTicks * 10.0F) - 1);

        //Check if block has been broken
        if (state.getPlayerRelativeBlockHardness(fakePlayer, world, pos) * miningTicks > 1.0f) {
            //Broken
            miningTicks = 0;
            this.blockSoundTimer = 0;
            world.playEvent(2001, pos, Block.getStateId(state));

            ItemStack itemstack = this.getActiveItemStack();
            if (itemstack.getItem().onBlockStartBreak(itemstack, pos, fakePlayer)) {
                return;
            }


            boolean harvest = state.getBlock().canHarvestBlock(world, pos, fakePlayer);

            itemstack.onBlockDestroyed(world, state, pos, fakePlayer);

            state.getBlock().onBlockHarvested(world, pos, state, fakePlayer);

            if (state.getBlock().removedByPlayer(state, world, pos, fakePlayer, true)) {
                state.getBlock().onPlayerDestroy(world, pos, state);
            } else {
                harvest = false;
            }

            if (harvest) {
                state.getBlock().harvestBlock(world, fakePlayer, pos, state, world.getTileEntity(pos), itemstack);
            }
        }
    }

    private void resetMining() {
        miningTicks = 0;
        this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
        this.lastMinePos.down(255);
    }


    @Override
    public void onDeath(@Nonnull DamageSource cause) {
        super.onDeath(cause);

        if (!world.isRemote) {
            if (world.getMinecraftServer() != null) {
                world.getMinecraftServer().getPlayerList().sendMessage(cause.getDeathMessage(this));
            }
        }
        this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        compound.setTag(Reference.MODID + ":inventory", itemHandler.serializeNBT());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (compound.hasKey(Reference.MODID + ":inventory")) {
            itemHandler.deserializeNBT(compound.getCompoundTag(Reference.MODID + ":inventory"));
        }
    }

    private RayTraceResult rayTraceBlockEntity() {
        Entity pointedEntity = null;

        double reachDistance = (double) this.getBlockReachDistance();
        RayTraceResult raytrace = this.rayTrace(reachDistance);
        Vec3d eyePosition = this.getPositionEyes(1);

        boolean flag = false;

        //Defaults to reachdistance
        double distanceFromHit = reachDistance;

        if (reachDistance > 3.0D) {
            flag = true;
        }

        if (raytrace != null) {
            distanceFromHit = raytrace.hitVec.distanceTo(eyePosition);
        }

        Vec3d lookVector = this.getLook(1.0F);
        Vec3d scaledLookVector = eyePosition.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);

        Vec3d entityPos = null;

        @SuppressWarnings("Guava") List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, e -> e != null && e.canBeCollidedWith()));
        double minEntityDist = distanceFromHit;

        for (Entity entity1 : list) {
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double) entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(eyePosition, scaledLookVector);

            if (axisalignedbb.contains(eyePosition)) {
                if (minEntityDist >= 0.0D) {
                    pointedEntity = entity1;
                    entityPos = raytraceresult == null ? eyePosition : raytraceresult.hitVec;
                    minEntityDist = 0.0D;
                }
            } else if (raytraceresult != null) {
                double distanceToEntity = eyePosition.distanceTo(raytraceresult.hitVec);

                if (distanceToEntity < minEntityDist || minEntityDist == 0.0D) {
                    if (entity1.getLowestRidingEntity() == this.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                        if (minEntityDist == 0.0D) {
                            pointedEntity = entity1;
                            entityPos = raytraceresult.hitVec;
                        }
                    } else {
                        pointedEntity = entity1;
                        entityPos = raytraceresult.hitVec;
                        minEntityDist = distanceToEntity;
                    }
                }
            }
        }

        if (pointedEntity != null && flag && eyePosition.distanceTo(entityPos) > 3.0D) {
            pointedEntity = null;
            raytrace = new RayTraceResult(RayTraceResult.Type.MISS, entityPos, EnumFacing.DOWN, new BlockPos(entityPos));
        }

        if (pointedEntity != null && (minEntityDist < distanceFromHit || raytrace == null)) {
            raytrace = new RayTraceResult(pointedEntity, entityPos);
        }
        return raytrace;
    }

    private RayTraceResult rayTrace(double blockReachDistance) {
        Vec3d vec3d = this.getPositionEyes(1);
        Vec3d vec3d1 = this.getLook(1);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

    private float getBlockReachDistance() {
        float attrib = (float) this.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        return attrib - 0.5F;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemHandler;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedItemIndex = selectedIndex;
        this.setHeldItem(EnumHand.MAIN_HAND, this.itemHandler.getStackInSlot(selectedIndex));
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, itemHandler.getOffhand());
        buffer.writeInt(selectedItemIndex);
        ByteBufUtils.writeItemStack(buffer, itemHandler.getStackInSlot(selectedItemIndex));
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        itemHandler.setStackInSlot(itemHandler.getOffhandSlot(), ByteBufUtils.readItemStack(additionalData));
        itemHandler.setStackInSlot(additionalData.readInt(), ByteBufUtils.readItemStack(additionalData));
    }
}
