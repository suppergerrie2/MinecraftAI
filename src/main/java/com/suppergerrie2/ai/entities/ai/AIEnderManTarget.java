package com.suppergerrie2.ai.entities.ai;

import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.Sys;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AIEnderManTarget extends EntityAINearestAttackableTarget<EntityMan> {
    static final Method teleportRandomly = ObfuscationReflectionHelper.findMethod(EntityEnderman.class, "func_70820_n", boolean.class);
    static final Method teleportToEntity = ObfuscationReflectionHelper.findMethod(EntityEnderman.class, "func_70816_c", boolean.class,  Entity.class);

    private final EntityEnderman taskOwner;
    /**
     * The player
     */
    private EntityMan man;
    private int aggroTime;
    private int teleportTime;

    public AIEnderManTarget(EntityEnderman taskOwner) {
        super(taskOwner, EntityMan.class, false);
        this.taskOwner = taskOwner;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        double targetDistance = this.getTargetDistance();

        for (EntityMan man : this.taskOwner.world.getEntitiesWithinAABB(EntityMan.class, this.taskOwner.getEntityBoundingBox().grow(targetDistance))) {
            if (shouldAttackMan(man)) {
                this.man = man;
            }
        }
        System.out.println(this.man);
        return this.man != null;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.aggroTime = 5;
        this.teleportTime = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask() {
        this.man = null;
        super.resetTask();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean shouldContinueExecuting() {
        if (this.man != null) {
            if (!this.shouldAttackMan(this.man)) {
                return false;
            } else {
                this.taskOwner.faceEntity(this.man, 10.0F, 10.0F);
                return true;
            }
        } else {
            return this.targetEntity != null && ((EntityMan) this.targetEntity).isEntityAlive() || super.shouldContinueExecuting();
        }
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask() {
        try {
            if (this.man != null) {
                if (--this.aggroTime <= 0) {
                    this.targetEntity = this.man;
                    this.man = null;
                    super.startExecuting();
                }
            } else {
                if (this.targetEntity != null) {
                    if (this.shouldAttackMan(this.targetEntity)) {
                        if (this.targetEntity.getDistanceSq(this.taskOwner) < 16.0D) {

                            teleportRandomly.invoke(this.taskOwner);
//                        this.taskOwner.teleportRandomly();
                        }

                        this.teleportTime = 0;
                    } else if (this.targetEntity.getDistanceSq(this.taskOwner) > 256.0D && this.teleportTime++ >= 30 && (boolean)teleportToEntity.invoke(this.taskOwner, this.targetEntity)) {
                        this.teleportTime = 0;
                    }
                }

                super.updateTask();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldAttackMan(EntityMan man) {

        ItemStack itemstack = man.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

        if (itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
            return false;
        } else {
            Vec3d vec3d = man.getLook(1.0F).normalize();
            Vec3d vec3d1 = new Vec3d(taskOwner.posX - man.posX, taskOwner.getEntityBoundingBox().minY + (double) taskOwner.getEyeHeight() - (man.posY + (double) man.getEyeHeight()), taskOwner.posZ - man.posZ);
            double d0 = vec3d1.length();
            vec3d1 = vec3d1.normalize();
            double d1 = vec3d.dotProduct(vec3d1);
            return d1 > 1.0D - 0.025D / d0 && man.canEntityBeSeen(taskOwner);
        }
    }
}