package com.suppergerrie2.ai.entities;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.suppergerrie2.ChaosNetClient.ChaosNetClient;
import com.suppergerrie2.ChaosNetClient.components.FitnessRule;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.ai.MinecraftAI;
import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.chaosnet.ChaosNetManager;
import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import com.suppergerrie2.ai.chaosnet.neurons.CraftOutputNeuron;
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
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EntityMan extends EntityLiving implements IEntityAdditionalSpawnData {

	public boolean playerTexturesLoaded = false;
	public boolean isTextureLoading = false;
	public Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
	public String skinType;

	public FakePlayer fakePlayer;

	private int miningTicks = 0;
	private BlockPos lastMinePos = BlockPos.ORIGIN.down();
	private float blockSoundTimer;
	private boolean lastTickLeftClicked = false;

	private final ItemHandlerMan itemHandler;
	private GameProfile profile;

	public boolean leftClicking;
	public boolean rightClicking;
	int rightClickDelay = 0;

	private int selectedItemIndex = 0;
	public SupperCraftOrganism organism;

	double desiredPitch;
	double desiredYaw;

	ChaosNetClient client = new ChaosNetClient();

	@SuppressWarnings("unused") //This constructor is needed for forge to work
	public EntityMan(World worldIn) {
		this(worldIn, "BOT");
	}

	@Deprecated
	public EntityMan(World worldIn, String name) {
		super(worldIn);
		this.enablePersistence();
		
		Random r = new Random();
		int x = r.nextInt(100);

		if (name.length() == 0) name = "BOT";
		
		if(x == 50) name = "MechanistPlays";
		if(x == 49) name = "SupperGerrie2";

		
		this.setCustomNameTag(name);
		
		setAlwaysRenderNameTag(true);

		profile = new GameProfile(null, name);

		this.setAIMoveSpeed(0.3f);

		itemHandler = new ItemHandlerMan(this);
		this.organism = null;

		this.moveHelper = new EntityMoveHelper(this) {
			@Override
			public void onUpdateMoveHelper() {
				//NO-OP this so it doesnt reset move forward
			}
		};

	}

	public EntityMan(World world, Organism organism) {
		this(world, organism.getName());

		this.organism = (SupperCraftOrganism) organism;
		this.organism.setOwner(this);
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
		//Updates Animations - By Mechanist
		updateAction();

		if (!this.world.isRemote && this.organism == null) {
			this.setDead();
			return;
		}

		int time = this.ticksExisted / 20;

		if (time >= 30 && !world.isRemote) {
			if (!this.isDead) {
				ChaosNetManager.reportOrganism(organism);
			}

			this.setDead();
			return;
		}

		super.onUpdate();

		if (fakePlayer == null && !world.isRemote) {
			fakePlayer = new FakePlayer((WorldServer) this.world, profile, this);
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				fakePlayer.inventory.setInventorySlotContents(i, itemHandler.getStackInSlot(i));
			}
		}



		if (this.isDead) {
			this.resetMining();
			return;
		}

		//Only evaluate networks on server, the server should decide what to do.
		if (!world.isRemote) {
			OutputNeuron[] networkOutput = organism.evaluate();

			//Forward and strafe will be changed based on the neural networks output
			double forward = 0;
			double strafe = 0;

			//We need to check every output to decide what to do.
			for (OutputNeuron output : networkOutput) {
				//Check the type of the output and do something based on that output's value
				switch (output.getType()) {
				case "JumpOutput":
					if (output.value > 0.5) {
						this.jumpHelper.setJumping();
					}
					break;
				case "CraftOutput":
					String recipeID = ((CraftOutputNeuron) output).recipeID;
					break;
				case "TurnPitchOutput":
					desiredPitch = MathHelper.clamp(output.value * 2 - 1, -1, 1) * 90;

					break;
				case "TurnYawOutput":
					desiredYaw = MathHelper.clamp(output.value * 2 - 1, -1, 1) * 180;

					break;
				case "WalkSidewaysOutput":
					if (output.value > 0.5) {
						strafe = 1 * output.value;
					} else if (output.value < 0.5) {
						strafe = -1 * output.value;
					}
					break;
				case "WalkForwardOutput":
					if (output.value > 0.5) {
						forward = 1 * output.value;
					} else if (output.value < 0.5) {
						forward = -1 * output.value;
					}
					break;
				case "LeftClickOutput":
					leftClicking = output.value > 0.5;
					break;
				case "RightClickOutput":
					rightClicking = output.value > 0.5;
					break;
				default:
					System.out.println("Unknown output type " + output.getType());
				}
			}

			//Calculate the block it wants to look at
			double yOffset = Math.sin(Math.toRadians(desiredPitch));
			double zOffset = Math.cos(Math.toRadians(desiredYaw)) * Math.cos(Math.toRadians(desiredPitch));
			double xOffset = Math.sin(Math.toRadians(desiredYaw)) * Math.cos(Math.toRadians(desiredPitch));

			this.getLookHelper().setLookPosition(posX + xOffset, posY + this.getEyeHeight() + yOffset, posZ + zOffset, 360, 360);
			this.renderYawOffset = 0;
			this.setRotation(this.rotationYawHead, this.rotationPitch);

			//Set the strafing and forward values with a max value of 1 and a min value of -1. This way the bot can decide how fast to run without teleporting through walls
			this.moveStrafing = (float) MathHelper.clamp(strafe, -1, 1);
			this.moveForward = (float) MathHelper.clamp(forward, -1, 1);

			//We use the rightclickdelay so the bot cant right click every tick.
			if (rightClickDelay > 0) rightClickDelay--;

			//Make sure that if the bot chooses to hold a different slot it updates its hand
			this.setHeldItem(EnumHand.MAIN_HAND, this.itemHandler.getStackInSlot(selectedItemIndex));

			//Make sure the fakeplayer is up to date with the positions
			fakePlayer.setPositionAndRotation(posX, posY, posZ, rotationYaw, rotationPitch);
			fakePlayer.onUpdate();

			//Check what the bot can see in front of him
			RayTraceResult result = this.rayTraceBlockEntity(0, 0);

			if (leftClicking) {
				leftClick(result);
			} else {

				//Needed to know so they pause between attacking mobs
				lastTickLeftClicked = false;

				//If lastMinePos isn't set at a negative y we just mined a block and need to reset it
				if (this.lastMinePos.getY() > 0) {
					resetMining();
				}
			}

			//When they want to right click and the delay is 0 right click
			if (rightClicking && rightClickDelay == 0) {
				rightClick(result);
			} else if (this.isHandActive() || fakePlayer.isHandActive()) { //If the fakeplayer or the bot is using an item reset it
				stopActiveHand();
				fakePlayer.stopActiveHand();
			}

			//Get all of the items in a range 1 block around it.
			List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(1.0D, 0.0D, 1.0D));

			//And try to pickup every item in range
			for (EntityItem item : items) {
				pickup(item);
			}
		}

	}

	//Adds swinging animation - By Mechanist
	private void updateAction() {
		this.updateArmSwingProgress();
	}


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


	//A lot of items are broken... Bows shoot, but it hits itself, enderpearls crash the game and I suspect a lot more items will be broken
	//But block placement works and some other items work as well
	//TODO: Entity interaction maybe?
	private void rightClick(RayTraceResult result) {
		this.rightClickDelay = 4;
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

				List<UUID> uuids = new ArrayList<>();

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

		if (itemstack.isEmpty()) return EnumActionResult.PASS;

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


			String debugMessage = this.getCustomNameTag() + " placed " + itemstack.getDisplayName();
			MinecraftAI.chat(world, debugMessage);

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
			//Block broken

			String debugMessage = this.getCustomNameTag() + " mined " + state.getBlock().getLocalizedName();
			MinecraftAI.chat(world, debugMessage);

			List<FitnessRule> fitnessRules = MinecraftAI.instance.session.getTrainingRoom().getFitnessRules("BLOCK_MINED");
			for (FitnessRule fitnessRule : fitnessRules) {
				if (fitnessRule.getAttributeID() == null || fitnessRule.getAttributeValue() == null) {
					//fitnessrule doesnt specify specific block, so it will give the score for each mined block
					organism.increaseScore(fitnessRule.getScoreEffect());
					//                    organism.increaseLive(fitnessRule.getLiveEffect());
				} else if (fitnessRule.getAttributeID().equals("BLOCK_ID")) {
					if (fitnessRule.getAttributeValue().equals(state.getBlock().getRegistryName().toString())) {
						organism.increaseScore(fitnessRule.getScoreEffect());
						//                    organism.increaseLive(fitnessRule.getLiveEffect());
					}
				} else {
					System.out.println("Unknown fitness attribute id: " + fitnessRule.getAttributeID());
				}
			}

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

            ChaosNetManager.reportOrganism(this.organism);
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

    public RayTraceResult rayTraceBlockEntity(float rotatePitch, float rotateYaw) {
        Entity pointedEntity = null;

        double reachDistance = (double) this.getBlockReachDistance();
        RayTraceResult raytrace = this.rayTrace(reachDistance, rotatePitch, rotateYaw);
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

        double yOffset = Math.sin(Math.toRadians((-rotationPitch) + rotatePitch));
        double zOffset = Math.cos(Math.toRadians((-rotationYaw) + rotateYaw)) * Math.cos(Math.toRadians(-rotationPitch + rotatePitch));
        double xOffset = Math.sin(Math.toRadians((-rotationYaw) + rotateYaw)) * Math.cos(Math.toRadians(-rotationPitch + rotatePitch));

        Vec3d lookVector = new Vec3d(xOffset, yOffset, zOffset);

        Vec3d scaledLookVector = eyePosition.add(xOffset * reachDistance, yOffset * reachDistance, zOffset * reachDistance);

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

        if (raytrace == null || raytrace.typeOfHit == null) {
            raytrace = new RayTraceResult(RayTraceResult.Type.MISS, this.getPositionVector(), EnumFacing.DOWN, this.getPosition());
        }

        return raytrace;
    }

    private RayTraceResult rayTrace(double blockReachDistance, float rotatePitch, float rotateYaw) {
        Vec3d vec3d = this.getPositionEyes(1);
        double yOffset = Math.sin(Math.toRadians((-rotationPitch) + rotatePitch));
        double zOffset = Math.cos(Math.toRadians((-rotationYaw) + rotateYaw)) * Math.cos(Math.toRadians(-rotationPitch + rotatePitch));
        double xOffset = Math.sin(Math.toRadians((-rotationYaw) + rotateYaw)) * Math.cos(Math.toRadians(-rotationPitch + rotatePitch));

        Vec3d vec3d2 = vec3d.add(xOffset * blockReachDistance, yOffset * blockReachDistance, zOffset * blockReachDistance);
        RayTraceResult result = this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);

        return result;
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

    @Override
    public void setDead() {

        this.resetMining();

        super.setDead();
    }

    //TODO
    private void addScore() {

    }
}
