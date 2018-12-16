package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.inventory.ItemHandlerMan;

import net.minecraft.client.Minecraft;
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
	    	
	    Entity man = (EntityMan) serverPlayer.world.getEntityByID(message.entityID);
	    	
	    if (man instanceof EntityMan) {
           if( ((EntityMan) man).leftClicking) {
				   
        	   ((EntityMan) man).leftClicking = false;
				   
				  } else if( ((EntityMan) man).leftClicking==false) {
					  
					  ((EntityMan) man).leftClicking=true;
				  }
        }
	    });


		return null;
	}
}
