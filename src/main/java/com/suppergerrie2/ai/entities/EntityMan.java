package com.suppergerrie2.ai.entities;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityMan extends EntityLiving {

	FakePlayer fakePlayer;

	int miningTicks = 0;

	BlockPos lastMinePos = BlockPos.ORIGIN;

	private float blockSoundTimer;
	
	boolean lastTickLeftClicked = false;

	public boolean leftClicking;

	public EntityMan(World worldIn) {
		this(worldIn, "BOT");
	}
	
	public EntityMan(World worldIn, String name) {
		super(worldIn);
		if(!worldIn.isRemote) {
			fakePlayer = new FakePlayer((WorldServer)this.world, new GameProfile(this.getUniqueID(), name));
		}

		this.setAIMoveSpeed(0.3f);
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
		this.activeItemStack = new ItemStack(Items.DIAMOND_SWORD);
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
		
		if(this.isDead) {
			this.resetMining();
			return;
		}
		
		if(!world.isRemote) {
			//TODO: Sync whole inventory with fakeplayer
			fakePlayer.inventory.setInventorySlotContents(0, getActiveItemStack());
			fakePlayer.setPosition(posX, posY, posZ);
			fakePlayer.onUpdate();
			
			

			RayTraceResult result = this.rayTraceBlockEntity();

			if(leftClicking) {
				leftClick(result);
			} else {
				lastTickLeftClicked = false;
			}
		}
	}

	public void leftClick(RayTraceResult result) {
	
		if(result==null) return;
	
		switch(result.typeOfHit) {
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
				state.getBlock().onBlockDestroyedByPlayer(world, pos, state);
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
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
	
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
		Vec3d scaledLookVector = eyePosition.addVector(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);
		
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
	    Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
	    return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
	}

	private float getBlockReachDistance() {
		float attrib = (float) this.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		return attrib - 0.5F;
	}
}
