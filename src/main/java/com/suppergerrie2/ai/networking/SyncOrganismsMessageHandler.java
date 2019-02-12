package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncOrganismsMessageHandler implements IMessageHandler<SyncOrganismsMessage, IMessage> {


    @Override
    public IMessage onMessage(SyncOrganismsMessage message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {

            for(SupperCraftOrganism o : message.organisms) {
                Entity e = Minecraft.getMinecraft().world.getEntityByID(o.ownerId);

                o.setOwner(e instanceof EntityMan ? (EntityMan) e : null);
            }

            TileEntity e = Minecraft.getMinecraft().world.getTileEntity(message.blockHubPos);
            if(e instanceof TileEntityBotHub) {
                TileEntityBotHub botHub = (TileEntityBotHub) e;
                botHub.organismsSpawned = message.organisms;
            }

        });

        return null;
    }
}
