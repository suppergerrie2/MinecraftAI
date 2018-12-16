package com.suppergerrie2.ai.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MineAndPlace implements IMessage{

    public enum Action {TOGGLE_MINE, TOGGLE_PLACE}

    int entityID;
    Action action;

	public MineAndPlace() {}

    public MineAndPlace(int entityID, Action action) {
		 this.entityID = entityID;
        this.action = action;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
        entityID = buf.readInt();
        action = Action.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		 buf.writeInt(entityID);
        buf.writeInt(action.ordinal());
	}

}
