package com.suppergerrie2.ai;

import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void spawnEvent(EntityJoinWorldEvent event) {
        //TODO: Enderman needs a special target task, not yet added. Same for pigman
        if (event.getEntity() instanceof EntityMob && !(event.getEntity() instanceof EntityEnderman || event.getEntity() instanceof EntityPigZombie)) {
            EntityMob zombie = (EntityMob) event.getEntity();

            zombie.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMan>(zombie, EntityMan.class, true));
        }
    }

}
