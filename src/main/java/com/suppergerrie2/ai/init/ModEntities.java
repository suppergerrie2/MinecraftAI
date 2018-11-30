package com.suppergerrie2.ai.init;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.entities.EntityMan;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@EventBusSubscriber(modid=Reference.MODID)
public class ModEntities {

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<EntityEntry> event) {
		EntityEntry e = EntityEntryBuilder.create().entity(EntityMan.class).name("man").tracker(80, 1, false).id(new ResourceLocation(Reference.MODID, "man"), 0).build();
		event.getRegistry().registerAll(e);
	}
	
}
