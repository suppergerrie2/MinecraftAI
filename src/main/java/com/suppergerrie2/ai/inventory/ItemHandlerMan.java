package com.suppergerrie2.ai.inventory;

import net.minecraftforge.items.ItemStackHandler;

public class ItemHandlerMan extends ItemStackHandler {

    public ItemHandlerMan() {
        //9*3 for main inventory, 9 for hotbar and 1 for offhand
        super(9 * 3 + 9 + 1);
    }


}
