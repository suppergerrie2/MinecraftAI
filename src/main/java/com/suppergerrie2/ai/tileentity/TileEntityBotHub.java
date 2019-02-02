package com.suppergerrie2.ai.tileentity;


import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileEntityBotHub extends TileEntity implements ITickable {

    private static final double spawnRange = 10;

    int targetAmount = 10;
    List<UUID> a = new ArrayList<>();
//    ChaosNetManager manager;

    int numRequested = 0;

    public void update() {

        //Make sure we can load organisms
        if (!MinecraftAI.instance.client.isAuthenticated() || MinecraftAI.instance.session == null) return;

        if (!world.isRemote && a.size() < targetAmount) {
            if (MinecraftAI.instance.manager.hasOrganisms()) {

                Organism organism = MinecraftAI.instance.manager.getOrganism();

                EntityMan man = new EntityMan(world, organism);
                man.setPosition((TileEntityBotHub.this.pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D),
                        TileEntityBotHub.this.pos.getY() + 1,
                        TileEntityBotHub.this.pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D);

                world.spawnEntity(man);
                a.add(man.getUniqueID());
                numRequested--;
            } else if (numRequested <= 0) {
                numRequested = targetAmount - a.size();
                MinecraftAI.instance.manager.requestOrganisms(numRequested);
            }

        }
//        if (manager != null && manager.hasOrganisms()) {
//            Organism organism = manager.getOrganism();
//
//            EntityMan man = new EntityMan(world, organism);
//            man.setPosition((TileEntityBotHub.this.pos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D),
//                    TileEntityBotHub.this.pos.getY() + 1,
//                    TileEntityBotHub.this.pos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double) spawnRange + 0.5D);
//
//            world.spawnEntity(man);
//            a.add(man.getUniqueID());
//        }
//
//        if (!world.isRemote && a.size() < 10 && (manager == null || manager.isDone())) {
//            manager = new ChaosNetManager(10 - a.size());
//            manager.start();
//        } else {
        a.removeIf((id) -> ((WorldServer) world).getEntityFromUuid(id) == null);
//        }
    }

}
