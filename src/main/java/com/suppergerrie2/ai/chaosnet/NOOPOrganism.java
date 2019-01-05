package com.suppergerrie2.ai.chaosnet;

import com.suppergerrie2.ChaosNetClient.components.nnet.NeuralNetwork;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;

public class NOOPOrganism extends SupperCraftOrganism {

    public static NOOPOrganism INSTANCE = new NOOPOrganism();

    private NeuralNetwork neuralNetwork = new NeuralNetwork();

    private NOOPOrganism() {
        neuralNetwork.buildStructure();
    }

    @Override
    public void setNetwork(NeuralNetwork neuralNetwork) {
    }

    @Override
    public OutputNeuron[] evaluate() {
        return new OutputNeuron[0];
    }

    @Override
    public String getTrainingRoomNamespace() {
        return "NOOPTrainingRoom";
    }

    @Override
    public String getNamespace() {
        return "NOOP";
    }

    @Override
    public String getName() {
        return "NOOP";
    }

    @Override
    public int getGeneration() {
        return -1;
    }

    @Override
    public String getOwnerUsername() {
        return "suppergerrie2";
    }

    @Override
    public String getSpeciesNamespace() {
        return "NOOPSpecies";
    }

    @Override
    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    @Override
    public Object getScore() {
        return super.getScore();
    }

    @Override
    public double getTimeToLive() {
        return Double.MAX_VALUE;
    }
}
