package com.suppergerrie2.ai;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.entities.ai.AIEnderManTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.*;
import net.minecraft.item.ItemSword;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void spawnEvent(EntityJoinWorldEvent event) {

        //TODO: Enderman needs a special target task, not yet added. Same for pigman
        if (event.getEntity() instanceof EntityMob) {
            EntityMob mob = (EntityMob) event.getEntity();

            if(mob instanceof EntityPigZombie) {
                //Doesn't need a custom task
            } else if (mob instanceof EntityEnderman) {
                mob.targetTasks.addTask(2, new AIEnderManTarget((EntityEnderman)mob));
            } else {
                mob.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(mob, EntityMan.class, true));
            }
        }
    }

}
