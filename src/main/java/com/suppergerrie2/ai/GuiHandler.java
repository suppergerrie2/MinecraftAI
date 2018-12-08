package com.suppergerrie2.ai;

import com.suppergerrie2.ai.client.gui.GuiManInventory;
import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.inventory.ContainerManInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case Reference.DEBUG_INV_ID:
                Entity e = world.getEntityByID(x);
                if (e instanceof EntityMan) {
                    return new ContainerManInventory(player.inventory, (EntityMan) e);
                } else {
                    System.err.println("Invalid entityID! Expected EntityMan but got " + e);
                    return null;
                }
        }

        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case Reference.DEBUG_INV_ID:
                Entity e = world.getEntityByID(x);
                if (e instanceof EntityMan) {
                    return new GuiManInventory(player.inventory, (EntityMan) e);
                } else {
                    System.err.println("Invalid entityID! Expected EntityMan but got " + e);
                    return null;
                }
        }


        return null;
    }


//	@Override
//	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//		return new ContainerHeadCrafter(player.inventory, (TileEntityHeadCrafter) world.getTileEntity(new BlockPos(x,y,z)));
//	}
//
//	@Override
//	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//		if(ID == Reference.GUIID) return new GuiHeadCrafter(player.inventory, (TileEntityHeadCrafter)world.getTileEntity(new BlockPos(x,y,z)));
//		return null;
//	}
}
