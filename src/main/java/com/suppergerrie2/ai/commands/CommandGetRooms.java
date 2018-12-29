package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandGetRooms extends CommandBase {

    @Override
    public String getName() {
        return "getrooms";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/getrooms";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            sender.sendMessage(new TextComponentString("No need to pass args! :P"));
            return;
        }

        TrainingRoom[] rooms = MinecraftAI.instance.client.getTrainingRooms();

        if (rooms != null) {
            for (TrainingRoom room : rooms) {
                sender.sendMessage(new TextComponentString("Room: " + room.roomName + " by " + room.ownerName));
            }
        } else {
            sender.sendMessage(new TextComponentString("Error while getting rooms!"));
        }
    }
}
