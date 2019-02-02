package com.suppergerrie2.ai.chaosnet.neurons;

import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.AbstractNeuron;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;

public class CraftOutputNeuron extends OutputNeuron {

    public String recipeID;

    @Override
    public AbstractNeuron parseFromJson(JsonObject object) {
        CraftOutputNeuron neuron = (CraftOutputNeuron)super.parseFromJson(object);

        neuron.recipeID = object.get("recipeId").getAsString();

        return neuron;
    }

//    @Override
//    public NeuralNetwork.Output getOutput(Organism owner) {
//        NeuralNetwork.Output output = super.getOutput(owner);
//
//        output.extraData.put("recipeID", recipeID);
//
//        return output;
//    }
}
