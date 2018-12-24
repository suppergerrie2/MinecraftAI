package com.suppergerrie2.ai;

import com.suppergerrie2.ChaosNetClient.ChaosNetClient;
import com.suppergerrie2.ai.commands.CommandCreateRoom;
import com.suppergerrie2.ai.commands.CommandGetRooms;
import com.suppergerrie2.ai.commands.CommandLogin;
import com.suppergerrie2.ai.init.ModBlocks;
import com.suppergerrie2.ai.networking.PacketHandler;
import com.suppergerrie2.ai.proxies.IProxy;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class MinecraftAI {

    @Instance
    public static MinecraftAI instance;

    @SidedProxy(modId = Reference.MODID, clientSide = "com.suppergerrie2.ai.proxies.ClientProxy", serverSide = "com.suppergerrie2.ai.proxies.ServerProxy")
    public static IProxy proxy;

    public static Logger logger;
    public static ChaosNetClient chaosNetClient = new ChaosNetClient();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        proxy.preInit(event);
        PacketHandler.registerMessages();
        logger.info("preInit");
        ModBlocks.init();
        
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("init");

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("postInit");
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        // register server commands

        event.registerServerCommand(new CommandLogin());
        event.registerServerCommand(new CommandGetRooms());
        event.registerServerCommand(new CommandCreateRoom());
    }

}
