package com.suppergerrie2.ai.entities;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;

public class EntityMan extends EntityLiving {

	FakePlayer fakePlayer;

	int miningTicks = 0;
	BlockPos lastMinePos = BlockPos.ORIGIN.down();
	private float blockSoundTimer;
	boolean lastTickLeftClicked = false;

	//9 for hotbar, 9*3 for inventory and 1 for offhand
	ItemStack[] inventory = new ItemStack[9 + 9*3];

	public boolean leftClicking;

	int selectedItemIndex = 0;

	public EntityMan(World worldIn) {
		this(worldIn, "BOT");
	}

	public EntityMan(World worldIn, String name) {
		super(worldIn);
		if(!worldIn.isRemote) {
			fakePlayer = new FakePlayer((WorldServer)this.world, new GameProfile(this.getUniqueID(), name), this);
		}

		this.setAIMoveSpeed(0.3f);

		for(int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
		}
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_SPEED);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.LUCK);
		this.getAttributeMap().registerAttribute(EntityPlayer.REACH_DISTANCE);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

        if (this.isDead) {
			this.resetMining();
			return;
		}

        if (!world.isRemote) {
			this.setHeldItem(EnumHand.MAIN_HAND, this.inventory[this.selectedItemIndex]);

            fakePlayer.setPosition(posX, posY, posZ);
			fakePlayer.onUpdate();

            RayTraceResult result = this.rayTraceBlockEntity();

			if(leftClicking) {
				leftClick(result);
			} else {
				lastTickLeftClicked = false;
				if(this.lastMinePos.getY()>0) {
					resetMining();
				}
			}

            List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(1.0D, 0.0D, 1.0D));

            for (EntityItem item : items) {
				pickup(item);
			}
		}
	}

    //TODO: Check if this works with different kind of items. But I'm going to make a gui for that first
	void pickup(EntityItem item) {
        if (item.cannotPickup()) return;

		ItemStack stack = item.getItem();

        for (int i = 0; i < this.inventory.length; i++) {
			if(stack.isEmpty()) break;

            ItemStack iStack = this.inventory[i];

            if (iStack.isEmpty()) {
				this.inventory[i] = stack.copy();
				stack.setCount(0);
				break;
				//TODO: How does it handle NBT?
			} else if(iStack.isStackable()&&ItemStack.areItemsEqual(stack, iStack)) {
				if(iStack.getCount() + stack.getCount() > iStack.getMaxStackSize()) {
					stack.setCount(iStack.getMaxStackSize()-iStack.getCount());
					iStack.setCount(iStack.getMaxStackSize());
				} else {
					iStack.setCount(iStack.getCount()+stack.getCount());
					stack.setCount(0);
				}
			}
		}

        this.setHeldItem(EnumHand.MAIN_HAND, this.inventory[this.selectedItemIndex]);

        if (stack.isEmpty()) {
			item.setDead();
		}
	}

	@Override
	public void setHeldItem(EnumHand hand, ItemStack stack)
    {
		if(stack!=this.getHeldItem(hand)) {
			super.setHeldItem(hand, stack);
			fakePlayer.setHeldItem(hand, stack);
		}
    }

    public void leftClick(RayTraceResult result) {

        if (result == null) return;

        switch (result.typeOfHit) {
		case BLOCK:
			mine(result.getBlockPos(), result.sideHit);
			break;
		case ENTITY:
			if(!lastTickLeftClicked) {
				//Item damage (like swords) isn't working yet because of the todo in FakePlayer
				fakePlayer.attackTargetEntityWithCurrentItem(result.entityHit);
			}
		case MISS:
		default:
			resetMining();
			break;
		}
		lastTickLeftClicked = true;
	}

    //TODO: Sounds
    public void mine(BlockPos pos, EnumFacing facing) {
		if(!this.world.getWorldBorder().contains(pos)||pos.distanceSq(getPosition())>this.getBlockReachDistance()*this.getBlockReachDistance()) {
			resetMining();
			return;
		}

		if(!lastMinePos.equals(pos)) {
			resetMining();
		}

		lastMinePos = pos;

		miningTicks++;

		IBlockState state = world.getBlockState(pos);
		if (this.blockSoundTimer % 4.0F == 0.0F)
		{
			SoundType soundtype = state.getBlock().getSoundType(state, world, pos, this);
			this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0f)/8.0f, soundtype.getPitch() * 0.5f, false);
		}

		++this.blockSoundTimer;


		this.world.sendBlockBreakProgress(this.getEntityId(), pos, (int)(state.getPlayerRelativeBlockHardness(fakePlayer, world, pos)*miningTicks * 10.0F) - 1);

		//Check if block has been broken
		if(state.getPlayerRelativeBlockHardness(fakePlayer, world, pos)*miningTicks > 1.0f) {
			//Broken
			miningTicks = 0;
			this.blockSoundTimer = 0;
			world.playEvent(2001, pos, Block.getStateId(state));

			ItemStack itemstack = this.getActiveItemStack();
			if(itemstack.getItem().onBlockStartBreak(itemstack, pos, fakePlayer)) {
				return;
			}


			boolean harvest = state.getBlock().canHarvestBlock(world, pos, fakePlayer);

			itemstack.onBlockDestroyed(world, state, pos, fakePlayer);

			state.getBlock().onBlockHarvested(world, pos, state, fakePlayer);

			if(state.getBlock().removedByPlayer(state, world, pos, fakePlayer, true)) {
				state.getBlock().onPlayerDestroy(world, pos, state);
			} else {
				harvest = false;
			}

			if(harvest) {
				state.getBlock().harvestBlock(world, fakePlayer, pos, state, world.getTileEntity(pos), itemstack);
			}
		}
	}

	void resetMining() {
		miningTicks = 0;
		this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
		this.lastMinePos.down(255);
	}


	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		world.getMinecraftServer().getPlayerList().sendMessage(cause.getDeathMessage(this));
		this.world.sendBlockBreakProgress(this.getEntityId(), lastMinePos, -1);
	}


    public RayTraceResult rayTraceBlockEntity()
	{
		Entity pointedEntity = null;

		double reachDistance = (double)this.getBlockReachDistance();
		RayTraceResult raytrace  = this.rayTrace(reachDistance);
		Vec3d eyePosition = this.getPositionEyes(1);

		boolean flag = false;

        //Defaults to reachdistance
		double distanceFromHit = reachDistance;

		if (reachDistance > 3.0D)
		{
			flag = true;
		}

		if (raytrace != null)
		{
			distanceFromHit = raytrace.hitVec.distanceTo(eyePosition);
		}

		Vec3d lookVector = this.getLook(1.0F);
		Vec3d scaledLookVector = eyePosition.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);

        Vec3d entityPos = null;

        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
		{
			public boolean apply(@Nullable Entity p_apply_1_)
			{
				return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
			}
		}));
		double minEntityDist = distanceFromHit;

		for (int j = 0; j < list.size(); ++j)
		{
			Entity entity1 = list.get(j);
			AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
			RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(eyePosition, scaledLookVector);

			if (axisalignedbb.contains(eyePosition))
			{
				if (minEntityDist >= 0.0D)
				{
					pointedEntity = entity1;
					entityPos = raytraceresult == null ? eyePosition : raytraceresult.hitVec;
					minEntityDist = 0.0D;
				}
			}
			else if (raytraceresult != null)
			{
				double distanceToEntity = eyePosition.distanceTo(raytraceresult.hitVec);

				if (distanceToEntity < minEntityDist || minEntityDist == 0.0D)
				{
					if (entity1.getLowestRidingEntity() == this.getLowestRidingEntity() && !entity1.canRiderInteract())
					{
						if (minEntityDist == 0.0D)
						{
							pointedEntity = entity1;
							entityPos = raytraceresult.hitVec;
						}
					}
					else
					{
						pointedEntity = entity1;
						entityPos = raytraceresult.hitVec;
						minEntityDist = distanceToEntity;
					}
				}
			}
		}

		if (pointedEntity != null && flag && eyePosition.distanceTo(entityPos) > 3.0D)
		{
			pointedEntity = null;
			raytrace = new RayTraceResult(RayTraceResult.Type.MISS, entityPos, (EnumFacing)null, new BlockPos(entityPos));
		}

		if (pointedEntity != null && (minEntityDist < distanceFromHit || raytrace == null))
		{
			raytrace = new RayTraceResult(pointedEntity, entityPos);
		}
		return raytrace;
	}

	public RayTraceResult rayTrace(double blockReachDistance)
	{
	    Vec3d vec3d = this.getPositionEyes(1);
	    Vec3d vec3d1 = this.getLook(1);
		Vec3d vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
	    return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
	}

	private float getBlockReachDistance() {
		float attrib = (float) this.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		return attrib - 0.5F;
	}
}
