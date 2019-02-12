package com.suppergerrie2.ai.client.gui;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiBotHub extends GuiScreen {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/bothubgui.jpg");
	int guiHeight = 119;
	int guiWidth = 197;
	private TileEntityBotHub bothub;
	private int guiLeft;
	private int guiTop;


	public GuiBotHub(TileEntityBotHub bothub) {
		this.bothub = bothub;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
		
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth, guiHeight);

		for(int i = 0; i < bothub.organismsSpawned.size(); i++) {
			Organism organism = bothub.organismsSpawned.get(i);
			this.drawString(this.fontRenderer, organism.getName() + "-" + organism.getGeneration(), guiLeft+5, guiTop + 5 + i * 10, 0xFFFF00);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;
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
