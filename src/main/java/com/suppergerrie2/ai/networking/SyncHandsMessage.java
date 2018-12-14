package com.suppergerrie2.ai.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SyncHandsMessage implements IMessage {

    ItemStack stack;
    int entityID;
    int inventoryIndex;
    int selectedIndex;

    public SyncHandsMessage() {
    }

    public SyncHandsMessage(ItemStack stack, int entityID, int inventoryIndex, int selectedIndex) {
        this.stack = stack;
        this.entityID = entityID;
        this.inventoryIndex = inventoryIndex;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
        entityID = buf.readInt();
        inventoryIndex = buf.readInt();
        selectedIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeInt(entityID);
        buf.writeInt(inventoryIndex);
        buf.writeInt(selectedIndex);
    }
}

