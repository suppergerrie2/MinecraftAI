package com.suppergerrie2.ai.inventory;

import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ItemHandlerMan extends ItemStackHandler {

    final EntityMan owner;

    public ItemHandlerMan(EntityMan owner) {
        //9*3 for main inventory, 9 for hotbar and 1 for offhand
        super(9 * 3 + 9 + 1);
        this.owner = owner;
    }

    public ItemStack getOffhand() {
        return this.getStackInSlot(getOffhandSlot());
    }

    public int getOffhandSlot() {
        return this.getSlots() - 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
//        if(slot == getOffhandSlot()) {
//
//        } else {
        if (owner.fakePlayer != null) {
            owner.fakePlayer.inventory.setInventorySlotContents(slot, getStackInSlot(slot));
        }
//        }
    }
}
