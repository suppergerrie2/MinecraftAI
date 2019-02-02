package com.suppergerrie2.ai.tileentity;


import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;
import com.suppergerrie2.ai.chaosnet.ChaosNetManager;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileEntityBotHub extends TileEntity implements ITickable {

    private static final double spawnRange = 10;
    List<UUID> a = new ArrayList<>();
    ChaosNetManager manager;

    public void update() {

        //Make sure we can load organisms
        if (!MinecraftAI.instance.client.isAuthenticated() || MinecraftAI.instance.session == null) return;

        if (manager != null && manager.hasOrganisms()) {
            Organism organism = manager.getOrganism();

            EntityMan man = new EntityMan(world, organism);
            man.setPosition((this.pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D),
                    this.pos.getY() + 1,
                    this.pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D);

            if (man.getCanSpawnHere() && man.isNotColliding()) {
                world.spawnEntity(man);
                a.add(man.getUniqueID());
            } else {
                manager.addFailedSpawn(organism);
            }
        }

        if (!world.isRemote && a.size() < 10 && (manager == null || manager.isDone())) {
            manager = new ChaosNetManager(10 - a.size());
            manager.start();
        } else {
            a.removeIf((id) -> ((WorldServer) world).getEntityFromUuid(id) == null);
        }
    }

}
