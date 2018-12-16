package com.suppergerrie2.ai.client.gui;

import java.io.IOException;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.inventory.ContainerManInventory;
import com.suppergerrie2.ai.networking.MineAndPlace;
import com.suppergerrie2.ai.networking.PacketHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;
import scala.actors.remote.SendTo;

public class GuiManInventory extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/inventory.png");
	private static final int mou = 0;
    private final InventoryPlayer player;
    private final EntityMan entityMan;
    private GuiButton mine;
    private GuiButton place;
    
    PacketHandler h = new PacketHandler();
    
    public GuiManInventory(InventoryPlayer inventory, EntityMan e) {
        super(new ContainerManInventory(inventory, e));

        player = inventory;
        entityMan = e;
        this.xSize = 175;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
       this.buttonList.clear();
       this.mine = this.addButton(new GuiButton( 0, this.guiLeft + 79, this.guiTop + 7, 90, 20, "Mine"));
       this.place = this.addButton(new GuiButton( 1, this.guiLeft + 79, this.guiTop + 27, 90, 20, "Place"));
       this.mine.enabled=true;
       this.place.enabled=true;
       
       
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
    
   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
	   if (button.enabled)
       {
		   if (button.id == 0) {
			   
			   EntityMan man = this.entityMan;
			   
			   
			   
			   h.INSTANCE.sendToServer(new MineAndPlace(man.getEntityId()));
			   return;
		   }
       }
	   super.actionPerformed(button);
   }
  
}
