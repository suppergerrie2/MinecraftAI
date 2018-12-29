package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandStartSession extends CommandBase {

    @Override
    public String getName() {
        return "startsession";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/startsession [trainingRoomOwner] [trainingRoomNameSpace]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            return;
        }

        if (MinecraftAI.instance.client == null) {
            sender.sendMessage(new TextComponentString("Please login first!"));
            return;
        }

        TrainingRoom room = MinecraftAI.instance.client.getTrainingRoom(args[0], args[1]);

        if (room != null) {
            sender.sendMessage(new TextComponentString("Room: " + room.roomName + " by " + room.ownerName));

            MinecraftAI.instance.session = MinecraftAI.instance.client.startSession(room);
            if (MinecraftAI.instance.session == null) {
                sender.sendMessage(new TextComponentString("Error while starting session!"));
            } else {
                sender.sendMessage(new TextComponentString("Session " + MinecraftAI.instance.session.getNamespace() + "started!"));
            }
        } else {
            sender.sendMessage(new TextComponentString("Error while getting room!"));
        }
    }
}
