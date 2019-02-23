package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ai.MinecraftAI;
import com.suppergerrie2.ai.entities.EntityMan;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;
import java.io.FileNotFoundException;

public class CommandSpawnFile extends CommandBase {

    @Override
    public String getName() {
        return "spawn_file";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/spawn_file <fileName> [liveLeft]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {


        if (args.length != 1 && args.length != 2) {
            throw new SyntaxErrorException(getUsage(sender));
        }

        new Thread() {

            @Override
            public void run() {

                File file = new File(args[0]);

                Runnable fileNotFoundChat = () -> sender.sendMessage(new TextComponentTranslation("sminecraftai.commands.file_not_found", file.getAbsolutePath()));
                if (!file.exists()) {
                    server.addScheduledTask(fileNotFoundChat);
                    System.out.println("Cannot find file " + file.getAbsolutePath());
                    return;
                }

                try {
                    Organism organism = MinecraftAI.instance.client.loadOrganismFromFile(file.toPath());

                    if(args.length==2) {
                        try {
                            organism.liveLeft = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            server.addScheduledTask(() -> sender.sendMessage(new TextComponentTranslation("sminecraftai.commands.invalid_number", args[1])));
                        }
                    } else {
                        organism.liveLeft = 30;
                    }

                    server.addScheduledTask(() -> {
                        EntityMan man = new EntityMan(sender.getEntityWorld(), organism);
                        man.setPosition(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ());

                        sender.getEntityWorld().spawnEntity(man);
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    server.addScheduledTask(fileNotFoundChat);
                    return;
                }
            }

        }.start();
    }
}
