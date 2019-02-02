package com.suppergerrie2.ai;

import com.suppergerrie2.ChaosNetClient.ChaosNetClient;
import com.suppergerrie2.ChaosNetClient.components.Session;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import com.suppergerrie2.ai.chaosnet.neurons.CraftOutputNeuron;
import com.suppergerrie2.ai.chaosnet.neurons.EyeNeuron;
import com.suppergerrie2.ai.commands.CommandCreateRoom;
import com.suppergerrie2.ai.commands.CommandDumpRegistry;
import com.suppergerrie2.ai.commands.CommandGetRooms;
import com.suppergerrie2.ai.commands.CommandLogin;
import com.suppergerrie2.ai.commands.CommandStartSession;
import com.suppergerrie2.ai.init.ModBlocks;
import com.suppergerrie2.ai.networking.PacketHandler;
import com.suppergerrie2.ai.proxies.IProxy;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
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
    public ChaosNetClient client = new ChaosNetClient();
    public Session session = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        proxy.preInit(event);
        PacketHandler.registerMessages();
        logger.info("preInit");
        ModBlocks.init();

        client.registerCustomOrganismType(new SupperCraftOrganism());

        //Register neuron types:
        client.registerNeuronType("BlockPositionInput", new EyeNeuron());

        client.registerNeuronType("JumpOutput", new OutputNeuron());
        client.registerNeuronType("CraftOutput", new CraftOutputNeuron());
        client.registerNeuronType("TurnYawOutput", new OutputNeuron());
        client.registerNeuronType("TurnPitchOutput", new OutputNeuron());
        client.registerNeuronType("LeftClickOutput", new OutputNeuron());
        client.registerNeuronType("RightClickOutput", new OutputNeuron());
        client.registerNeuronType("WalkSidewaysOutput", new OutputNeuron());
        client.registerNeuronType("WalkForwardOutput", new OutputNeuron());

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
        event.registerServerCommand(new CommandStartSession());
        event.registerServerCommand(new CommandDumpRegistry());
    }

    public static void chat(World world, String message) {
        PlayerList players = world.getMinecraftServer().getPlayerList();
        players.sendMessage(
                new TextComponentString(message)
        );
    }

}
