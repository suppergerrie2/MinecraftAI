package com.suppergerrie2.ai.chaosnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.Organism;

public class SupperCraftOrganism extends Organism {

    public Eye[] eyes;

    @Override
    public void parseBiologyFromJson(JsonObject object) {
        JsonArray eyes = object.getAsJsonArray("eye");
        this.eyes = new Eye[eyes.size()];

        for(int i = 0; i < eyes.size(); i++) {
            JsonObject eye = eyes.get(i).getAsJsonObject();
            this.eyes[i] = new Eye();
            this.eyes[i].startDistance = eye.get("startDistance").getAsInt();
            this.eyes[i].distance = eye.get("distance").getAsInt();
            this.eyes[i].yaw = eye.get("yaw").getAsInt();
            this.eyes[i].pitch = eye.get("pitch").getAsInt();
        }
    }
}
