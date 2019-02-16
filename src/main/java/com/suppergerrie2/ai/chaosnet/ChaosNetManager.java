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

            Organism[] organismsToReportArray;
            synchronized (organismsToReport) {
                organismsToReportArray = organismsToReport.toArray(new Organism[0]);
            }

            Organism[] organismsReceived = MinecraftAI.instance.client.getOrganisms(MinecraftAI.instance.session, organismsToReportArray);
            organismsToReport.clear();

            for (Organism organism : organismsReceived) {
                organismsRequested--;
                organism.liveLeft = 30;
                organisms.add(organism);
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
        synchronized (organismsToReport) {
            if(organismsToReport.contains(o)) {
                System.err.println("Trying to report an organism that has already been reported! " + o.getNamespace());
                for(StackTraceElement i : Thread.currentThread().getStackTrace()) {
                    System.err.println(i);
                }
                return;
            }
            organismsToReport.add(o);
        }
    }

    public void addFailedSpawn(Organism organism) {
        this.organisms.add(organism);
    }
}
