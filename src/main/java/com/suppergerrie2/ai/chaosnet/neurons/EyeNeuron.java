package com.suppergerrie2.ai.chaosnet.neurons;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.AbstractNeuron;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.InputNeuron;
import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;

public class EyeNeuron extends InputNeuron {

    @SerializedName("attributeId")
    String attributeID;

    @SerializedName("attributeValue")
    String attributeValue;

    int eyeIndex;

    @Override
    public double getValue() {
        if(!(getOwner() instanceof SupperCraftOrganism)) {
            return 0;
        }

        SupperCraftOrganism organism = (SupperCraftOrganism) getOwner();

        return organism.eyes[eyeIndex].getValue(getOwner(), 0);
    }

    @Override
    public AbstractNeuron parseFromJson(JsonObject object) {
        EyeNeuron eye = (EyeNeuron)super.parseFromJson(object);

        eye.eyeIndex = object.get("eye").getAsJsonObject().get("index").getAsInt();

        return eye;
    }
}