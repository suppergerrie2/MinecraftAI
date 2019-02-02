package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class CommandCreateRoom extends CommandBase {

    @Override
    @Nonnull
    public String getName() {
        return "createroom";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/createroom roomname namespace simModelNamespace";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        if (args.length != 3) {
            //TODO: Translatable
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            return;
        }

        TrainingRoom room = new TrainingRoom(args[0], args[1], args[2]);
        if (MinecraftAI.instance.client.createTrainingRoom(room)) {
            sender.sendMessage(new TextComponentString("Room created!"));
        } else {
            sender.sendMessage(new TextComponentString("Room failed to create D:"));
        }
    }
}
