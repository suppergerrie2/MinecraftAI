package com.suppergerrie2.ai.networking;

import com.suppergerrie2.ai.Reference;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);

    private static int ID;

    public static void registerMessages() {
        INSTANCE.registerMessage(SyncHandsMessageHandler.class, SyncHandsMessage.class, ID++, Side.CLIENT);
    }

}
