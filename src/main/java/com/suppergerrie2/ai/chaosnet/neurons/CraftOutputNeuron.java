package com.suppergerrie2.ai.chaosnet.neurons;

import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.BasicNeuron;
import com.suppergerrie2.ChaosNetClient.components.nnet.NeuralNetwork;

public class CraftOutputNeuron extends BasicNeuron {

    int recipeID;

    @Override
    public BasicNeuron parseFromJson(JsonObject object) {
        CraftOutputNeuron neuron = (CraftOutputNeuron)super.parseFromJson(object);

        neuron.recipeID = object.get("recipeId").getAsInt();

        return neuron;
    }

    @Override
    public NeuralNetwork.Output getOutput(Organism owner) {
        NeuralNetwork.Output output = super.getOutput(owner);

        output.extraData.put("recipeID", recipeID);

        return output;
    }
}
