package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.inventory.ItemHandlerMan;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncHandsMessageHandler implements IMessageHandler<SyncHandsMessage, IMessage> {

    @Override
    public IMessage onMessage(SyncHandsMessage message, MessageContext ctx) {
        // Execute the action on the main server thread by adding it as a scheduled task
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Entity e = Minecraft.getMinecraft().world.getEntityByID(message.entityID);

            if (e instanceof EntityMan) {
                EntityMan man = (EntityMan) e;
                ((ItemHandlerMan) man.getItemHandler()).setStackInSlot(message.inventoryIndex, message.stack);
                man.setSelectedIndex(message.selectedIndex);
            }
        });
        // No response packet
        return null;
    }
}
