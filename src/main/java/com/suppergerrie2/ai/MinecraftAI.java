package com.suppergerrie2.ai;

import com.suppergerrie2.ChaosNetClient.ChaosNetClient;
import com.suppergerrie2.ai.commands.CommandCreateRoom;
import com.suppergerrie2.ai.commands.CommandGetRooms;
import com.suppergerrie2.ai.commands.CommandLogin;
import com.suppergerrie2.ai.proxies.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name=Reference.MODNAME, version=Reference.VERSION, acceptedMinecraftVersions=Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class MinecraftAI {

	@Instance
	public static MinecraftAI instance;
	
	@SidedProxy(modId=Reference.MODID,clientSide="com.suppergerrie2.ai.proxies.ClientProxy", serverSide="com.suppergerrie2.ai.proxies.ServerProxy")
	public static IProxy proxy;
	
	public static Logger logger;
	public static ChaosNetClient chaosNetClient = new ChaosNetClient();

	static int entityID = 0;
	
	public static final SimpleNetworkWrapper NETWORK_INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();

		proxy.preInit(event);

		logger.info("preInit");
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("init");
		
//		NetworkRegistry.INSTANCE.registerGuiHandler(instance, this);
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

//	@Override
//	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//		return new ContainerHeadCrafter(player.inventory, (TileEntityHeadCrafter) world.getTileEntity(new BlockPos(x,y,z)));
//	}
//
//	@Override
//	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//		if(ID == Reference.GUIID) return new GuiHeadCrafter(player.inventory, (TileEntityHeadCrafter)world.getTileEntity(new BlockPos(x,y,z)));
//		return null;
//	}
	
}
