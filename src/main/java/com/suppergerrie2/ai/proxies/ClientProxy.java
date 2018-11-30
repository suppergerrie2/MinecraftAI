package com.suppergerrie2.ai.proxies;

import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.entities.render.RenderEntityMan;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements IProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		
		RenderingRegistry.registerEntityRenderingHandler(EntityMan.class, new IRenderFactory<EntityMan>() {

			@Override
			public Render<EntityMan> createRenderFor(RenderManager manager) {
				return new RenderEntityMan(manager); 
			}
		});
		
		
	}

	@Override
	public void init(FMLInitializationEvent event) {
		
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		
	}

}
