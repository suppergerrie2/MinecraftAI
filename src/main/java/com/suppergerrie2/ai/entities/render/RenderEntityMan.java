package com.suppergerrie2.ai.entities.render;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.entities.EntityMan;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderEntityMan extends RenderLiving<EntityMan> {

	public RenderEntityMan(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelBiped(), 0.5f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMan entity) {
		return new ResourceLocation(Reference.MODID, "man");
	}


}
