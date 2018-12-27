package com.suppergerrie2.ai.init;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.block.BotHubBlock;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;

@Mod.EventBusSubscriber(modid=Reference.MODID)
public class ModBlocks {

	static Block bot_hub;
	
	public static void init() {
		bot_hub = new BotHubBlock("bot_hub", Material.BARRIER);
		GameRegistry.registerTileEntity(TileEntityBotHub.class, "bot_hub");
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(bot_hub);
	}
	
	@SubscribeEvent
	public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(new ItemBlock(bot_hub).setRegistryName(bot_hub.getRegistryName()));

	}
	
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		 registerRender(Item.getItemFromBlock(bot_hub));
	}
	
	public static void registerRender(Item item) {
		  ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory"));

	}
}