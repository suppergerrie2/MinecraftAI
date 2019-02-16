package com.suppergerrie2.ai.chaosnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.ai.entities.EntityMan;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class SupperCraftOrganism extends Organism {

    public transient EntityMan owner;
    transient public Eye[] eyes;
    public final transient int ownerId; //Client only

    //For use on client
    public SupperCraftOrganism(int ownerID, String trainingRoomNamespace, String namespace, String name, int generation, String ownerUsername, String speciesNamespace, double score, double timeToLive, double liveLeft) {
        super(trainingRoomNamespace, namespace, name, generation, ownerUsername, speciesNamespace, null, score, timeToLive, liveLeft);

        this.ownerId = ownerID;
    }

    public SupperCraftOrganism() {
        this.ownerId = 0;
    }

    public void setOwner(EntityMan owner) {
        this.owner = owner;
    }

    @Override
    public OutputNeuron[] evaluate() {

        if (owner == null) {
            System.err.println("Cannot evaluate without owner!");
            return new OutputNeuron[0];
        }

        return super.evaluate();
    }

    @Override
    public void parseBiologyFromJson(JsonObject object) {
        JsonArray eyes = object.getAsJsonArray("eye");
        this.eyes = new Eye[eyes.size()];

        for (int i = 0; i < eyes.size(); i++) {
            JsonObject eye = eyes.get(i).getAsJsonObject();
            this.eyes[i] = new Eye();
            this.eyes[i].startDistance = eye.get("startDistance").getAsInt();
            this.eyes[i].distance = eye.get("distance").getAsInt();
            this.eyes[i].yaw = eye.get("yaw").getAsInt();
            this.eyes[i].pitch = eye.get("pitch").getAsInt();
        }
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(owner.getEntityId());
        ByteBufUtils.writeUTF8String(buf, getTrainingRoomNamespace());
        ByteBufUtils.writeUTF8String(buf, getNamespace());
        ByteBufUtils.writeUTF8String(buf, getName());
        buf.writeInt(getGeneration());
        ByteBufUtils.writeUTF8String(buf, getOwnerUsername());
        ByteBufUtils.writeUTF8String(buf, getSpeciesNamespace());
        buf.writeDouble(getScore());
        buf.writeDouble(getTimeToLive());
        buf.writeDouble(liveLeft);
    }

    public static SupperCraftOrganism fromBytes(ByteBuf buf) {

        int ownerID = buf.readInt();
        String trainingRoomNamespace = ByteBufUtils.readUTF8String(buf);
        String namespace = ByteBufUtils.readUTF8String(buf);
        String name = ByteBufUtils.readUTF8String(buf);
        int generation = buf.readInt();
        String ownerUsername = ByteBufUtils.readUTF8String(buf);
        String speciesNamespace = ByteBufUtils.readUTF8String(buf);
        double score = buf.readDouble();
        double timeToLive = buf.readDouble();
        double liveLeft = buf.readDouble();

        return new SupperCraftOrganism(ownerID, trainingRoomNamespace, namespace, name, generation, ownerUsername, speciesNamespace, score, timeToLive, liveLeft);
    }
}
