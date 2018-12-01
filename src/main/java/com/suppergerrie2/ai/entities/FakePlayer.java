package com.suppergerrie2.ai.entities;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.WorldServer;

public class FakePlayer extends net.minecraftforge.common.util.FakePlayer {

	public FakePlayer(WorldServer world, GameProfile name) {
		super(world, name);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.ticksSinceLastSwing++;

		//TODO: apply attributes
//		ItemStack stack = this.getHeldItemMainhand();
//		stack.getAttributeModifiers(equipmentSlot)

	}

}
