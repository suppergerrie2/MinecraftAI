package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MineAndPlaceHandler implements IMessageHandler<MineAndPlace, IMessage> {

    @Override
    public IMessage onMessage(MineAndPlace message, MessageContext ctx) {

        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

        serverPlayer.getServerWorld().addScheduledTask(() -> {

            Entity e = serverPlayer.world.getEntityByID(message.entityID);

            if (e instanceof EntityMan) {
                EntityMan man = (EntityMan) e;

                switch (message.action) {
                    case TOGGLE_MINE:
                        man.leftClicking = !man.leftClicking;
                        break;
                    case TOGGLE_PLACE:
                        //TODO: Placing!
                        break;
                }
            }
        });


        return null;
    }
}
