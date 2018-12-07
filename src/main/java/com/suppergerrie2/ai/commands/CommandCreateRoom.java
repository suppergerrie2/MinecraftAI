package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandCreateRoom extends CommandBase {

    @Override
    public String getName() {
        return "createroom";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/createroom roomname namespace";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            //TODO: Translatable
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            return;
        }

        TrainingRoom room = new TrainingRoom(args[0], args[1]);
        if (MinecraftAI.chaosNetClient.createTrainingRoom(room)) {
            sender.sendMessage(new TextComponentString("Room created!"));
        } else {
            sender.sendMessage(new TextComponentString("Room failed to create D:"));
        }
    }
}
