package com.suppergerrie2.ai.chaosnet;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.util.math.RayTraceResult;

public class Eye {

    public int startDistance;
    public int distance;
    public int yaw;
    public int pitch;

    public double getValue(Organism owner, String blockId) {
        SupperCraftOrganism supperCraftOrganism = (SupperCraftOrganism) owner;

        RayTraceResult result = supperCraftOrganism.owner.rayTraceBlockEntity(pitch, yaw);

        switch(result.typeOfHit) {
        case MISS:
        	return -1;
        case ENTITY:
        	return -1;
        case BLOCK:
        	if(supperCraftOrganism.owner.world.getBlockState(result.getBlockPos()).getBlock().getRegistryName().toString().equals(blockId)) {
        		double eyedist = supperCraftOrganism.owner.getDistanceSq(result.getBlockPos());

        		if(eyedist>startDistance*startDistance&&eyedist<distance*distance) {
                    MinecraftAI.chat(supperCraftOrganism.owner.world, owner.getName() + " sees " + blockId);
                    return 1 - (eyedist / (distance*distance));
                }
                return -1;
        	}
        }
        return -1;
    }
}
