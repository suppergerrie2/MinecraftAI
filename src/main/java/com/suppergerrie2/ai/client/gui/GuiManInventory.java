package com.suppergerrie2.ai.client.gui;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.inventory.ContainerManInventory;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiManInventory extends GuiContainer {
	

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/inventory.png");
    private final InventoryPlayer player;
    private final EntityMan entityMan;
    
    public GuiManInventory(InventoryPlayer inventory, EntityMan e) {
        super(new ContainerManInventory(inventory, e));

        player = inventory;
        entityMan = e;
        this.xSize = 176;
        this.ySize = 184;
    }

    @Override
    public void initGui() {
        super.initGui();
       
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        
        
    }

    @Override
    protected boolean checkHotbarKeys(int keyCode) {
        return false;
    }
    
  
}
