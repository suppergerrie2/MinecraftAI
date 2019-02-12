package com.suppergerrie2.ai.client.gui;

import java.io.IOException;

import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiBotHub extends GuiScreen {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/bothubgui.jpg");
	int guiHeight = 119;
	int guiWidth = 197;
	TileEntityBotHub bothub;
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
		
		int centX = (width / 2) - guiWidth / 2;
		int centY = (height / 2) - guiHeight / 2;
		drawTexturedModalRect(centX, centY, 0, 0, guiWidth, guiHeight);
		
		drawHoveringText("YEE", 0, 0);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void initGui() {
		super.initGui();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return super.doesGuiPauseGame();
	}
	
	@Override
	public void onGuiClosed() {
		// TODO Auto-generated method stub
		super.onGuiClosed();
	}
}
