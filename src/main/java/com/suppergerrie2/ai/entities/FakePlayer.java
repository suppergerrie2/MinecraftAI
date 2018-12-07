package com.suppergerrie2.ai.entities;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.world.WorldServer;

public class FakePlayer extends net.minecraftforge.common.util.FakePlayer {

    EntityMan masterMan;

    public FakePlayer(WorldServer world, GameProfile name, EntityMan entityMan) {
        super(world, name);
        this.masterMan = entityMan;

        for (IAttributeInstance ati : entityMan.getAttributeMap().getAllAttributes()) {
            if (this.getAttributeMap().getAttributeInstance(ati.getAttribute()) == null)
                this.getAttributeMap().registerAttribute(ati.getAttribute()).setBaseValue(ati.getBaseValue());
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.ticksSinceLastSwing++;

        for (IAttributeInstance attribute : masterMan.getAttributeMap().getAllAttributes()) {
            this.getAttributeMap().getAttributeInstance(attribute.getAttribute()).removeAllModifiers();
            for (AttributeModifier modifier : attribute.getModifiers()) {
                this.getAttributeMap().getAttributeInstance(attribute.getAttribute()).applyModifier(modifier);
            }
        }

        //TODO: apply attributes
//		ItemStack stack = this.getHeldItemMainhand();
//		stack.getAttributeModifiers(equipmentSlot)

    }

}
