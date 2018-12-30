package com.suppergerrie2.ai.chaosnet;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.BasicNeuron;

public class Eye {

    public int startDistance;
    public int distance;
    public int yaw;
    public int pitch;

    public double getValue(Organism owner) {
        //Ray cast
        return 1;
    }
}
