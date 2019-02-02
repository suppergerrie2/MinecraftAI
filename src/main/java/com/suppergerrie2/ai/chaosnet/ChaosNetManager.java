package com.suppergerrie2.ai.chaosnet;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ChaosNetManager extends Thread {

    ConcurrentLinkedQueue<Organism> organisms = new ConcurrentLinkedQueue<>();

    final static ConcurrentLinkedQueue<Organism> organismsToReport = new ConcurrentLinkedQueue<>();

    int organismsRequested = 0;

    public ChaosNetManager(int organismsRequested) {
        super("ChaosNetManager");
        this.organismsRequested = organismsRequested;
    }

    @Override
    public void run() {
        while (organismsRequested > 0 && MinecraftAI.instance.client.isAuthenticated() && MinecraftAI.instance.session != null) {

            synchronized (organismsToReport) {
                Organism[] organismsReceived = MinecraftAI.instance.client.getOrganisms(MinecraftAI.instance.session, organismsToReport.toArray(new Organism[0]));

                organismsToReport.clear();

                for (Organism organism : organismsReceived) {
                    organismsRequested--;
                    organisms.add(organism);
                }
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

    public static void reportOrganism(Organism o) {
        organismsToReport.add(o);
    }

    public void addFailedSpawn(Organism organism) {
        this.organisms.add(organism);
    }
}
