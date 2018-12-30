package com.suppergerrie2.ai.chaosnet.neurons;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.BasicNeuron;
import com.suppergerrie2.ai.chaosnet.Eye;
import com.suppergerrie2.ai.chaosnet.SupperCraftOrganism;

public class EyeNeuron extends BasicNeuron {

    @SerializedName("attributeId")
    String attributeID;

    @SerializedName("attributeValue")
    int attributeValue;

    int eyeIndex;

    @Override
    public double getValue(Organism owner) {
        if(!(owner instanceof SupperCraftOrganism)) {
            return 0;
        }

        SupperCraftOrganism organism = (SupperCraftOrganism) owner;

        return organism.eyes[eyeIndex].getValue(owner);
    }

    @Override
    public BasicNeuron parseFromJson(JsonObject object) {
        EyeNeuron eye = (EyeNeuron)super.parseFromJson(object);

        eye.eyeIndex = object.get("eye").getAsJsonObject().get("index").getAsInt();

        return eye;
    }
}