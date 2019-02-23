package com.suppergerrie2.ai.client.gui;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.Reference;
import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import com.suppergerrie2.ai.entities.EntityMan;
import com.suppergerrie2.ai.tileentity.TileEntityBotHub;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.floor;

public class GuiBotHub extends GuiScreen {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/bothubgui.jpg");
	int guiHeight = 164;
	int guiWidth = 246;
	private TileEntityBotHub bothub;
	private int guiLeft;
	private int guiTop;
	List<GuiButton> buttons = new ArrayList<GuiButton>();
	GuiButton button1;
    final int BUTTON1 = 0;



	public GuiBotHub(TileEntityBotHub bothub) {
		this.bothub = bothub;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, guiWidth, guiHeight);

		button1.drawButton(mc, mouseX, mouseY, partialTicks);
		for(GuiButton button: buttons){
			button.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.guiWidth) / 2;
		this.guiTop = (this.height - this.guiHeight) / 2;
        buttonList.add(button1 = new GuiButton(BUTTON1, (width / 2) - 100 / 2, guiTop + this.guiWidth - 30, 100, 20, "Close"));
        updateButtons();
	}

	public void updateButtons() {
		int btnCount = 1;
		buttons.clear();
		for(SupperCraftOrganism organism: bothub.organismsSpawned){
			AiGuiButton button;

			buttonList.add(button = new AiGuiButton(
							btnCount,
							guiLeft + 5,
                    ((btnCount * 15) + guiTop) - 10,
							50,
							15,
					"" + organism.getName()
					)
			);
            buttons.add(button);

            buttonList.add(button = new AiGuiButton(
                            btnCount,
                            guiLeft + 55,
                            ((btnCount * 15) + guiTop) - 10,
                            50,
                            15,
                            "" + organism.getScore()
                    )
            );
            buttons.add(button);

            buttonList.add(button = new AiGuiButton(
                            btnCount,
                            guiLeft + 105,
                            ((btnCount * 15) + guiTop) - 10,
                            50,
                            15,
                            "" + organism.getGeneration()
                    )
            );
            buttons.add(button);

            buttonList.add(button = new AiGuiButton(
                            btnCount,
                            guiLeft + 155,
                            ((btnCount * 15) + guiTop) - 10,
                            50,
                            15,
                            "" + floor(organism.liveLeft)
                    )
            );
            button.organism = organism;
			btnCount += 1;
			buttons.add(button);
		}


		//if (title.equals("Close"))  {
		button1.enabled = true;
        /*} else {
            button1.enabled = false;
        }*/
	}



	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);

        switch (button.id) {
            case BUTTON1:
                mc.displayGuiScreen(null);
                break;
        }
	}


	@Override
	public void onGuiClosed() {
		// TODO Auto-generated method stub
        updateButtons();
		super.onGuiClosed();
	}
}
