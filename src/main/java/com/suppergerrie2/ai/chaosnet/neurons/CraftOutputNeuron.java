package com.suppergerrie2.ai.chaosnet.neurons;

import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.nnet.BasicNeuron;

public class CraftOutputNeuron extends BasicNeuron {

    int recipeID;

    @Override
    public BasicNeuron parseFromJson(JsonObject object) {
        CraftOutputNeuron neuron = (CraftOutputNeuron)super.parseFromJson(object);

        neuron.recipeID = object.get("recipeId").getAsInt();

        return neuron;
    }
}
