package com.suppergerrie2.ai.chaosnet;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChaosNetManager extends Thread {

    ConcurrentLinkedQueue<Organism> organisms = new ConcurrentLinkedQueue<>();

    int organismsRequested = 0;

    public ChaosNetManager() {
        super("ChaosNetManager");
    }

    @Override
    public void run() {
        while (true) {
            if(MinecraftAI.instance.client.isAuthenticated() && MinecraftAI.instance.session!=null) {

                if (organismsRequested > 0) {
                    System.out.println("Requesting!");
                    Organism[] organismsReceived = MinecraftAI.instance.client.getOrganisms(MinecraftAI.instance.session);

                    for (Organism organism : organismsReceived) {
                        organismsRequested--;
                        organisms.add(organism);
                    }
                }
            }

            if(organismsRequested<0) {
                organismsRequested = 0;
            }
        }
    }

    public boolean hasOrganisms() {
        return !organisms.isEmpty();
    }

    public Organism getOrganism() {
        return organisms.poll();
    }

    public boolean isDone() {
        return organismsRequested <= 0 && !hasOrganisms();
    }

    public void requestOrganisms(int amount) {
        this.organismsRequested += amount;
    }
}
