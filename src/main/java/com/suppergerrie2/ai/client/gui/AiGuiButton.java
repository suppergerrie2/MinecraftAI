package com.suppergerrie2.ai.client.gui;

import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;
import net.minecraft.client.gui.GuiButton;

public class AiGuiButton extends GuiButton {
    protected SupperCraftOrganism organism;
    public String event;

    public AiGuiButton(int buttonId, int x, int y, int width, int height, String text) {
        super(buttonId, x, y, width, height, text);
    }

    public void setOrganism(SupperCraftOrganism organism) {
        this.organism = organism;
    }

    public SupperCraftOrganism getOrganism() {
        return this.organism;
    }
}
