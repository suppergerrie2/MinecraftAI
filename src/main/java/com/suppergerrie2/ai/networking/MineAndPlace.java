package com.suppergerrie2.ai.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MineAndPlace implements IMessage{
	
	public int entityID;
	
	public MineAndPlace() {}
	
	public MineAndPlace(int entityID) {
		 this.entityID = entityID;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		 entityID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		 buf.writeInt(entityID);
	}

}
