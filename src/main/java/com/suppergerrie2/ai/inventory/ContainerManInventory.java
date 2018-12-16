package com.suppergerrie2.ai.inventory;

import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerManInventory extends Container {

    private final EntityMan man;

    public ContainerManInventory(InventoryPlayer inventory, EntityMan e) {
        man = e;

        for (int x = 0; x < 9; x++) {
            this.addSlotToContainer(new SlotDisplay(e.getItemHandler(), x, 8 + x * 18, 142));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new SlotDisplay(e.getItemHandler(), 9 + x + y * 9, 8 + x * 18, 84 + y * 18));
            }
        }

        this.addSlotToContainer(new SlotDisplay(e.getItemHandler(), e.getItemHandler().getSlots() - 1, 77, 62));
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
