package com.suppergerrie2.ai.chaosnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.ai.entities.EntityMan;

public class SupperCraftOrganism extends Organism {

    transient EntityMan owner;
    transient public Eye[] eyes;

    public void setOwner(EntityMan owner) {
        this.owner = owner;
    }

    @Override
    public OutputNeuron[] evaluate() {

        if(owner==null) {
            System.err.println("Cannot evaluate without owner!");
            return new OutputNeuron[0];
        }

        return super.evaluate();
    }

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
