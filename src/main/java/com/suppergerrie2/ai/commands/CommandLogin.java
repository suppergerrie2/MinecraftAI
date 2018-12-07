package com.suppergerrie2.ai.commands;

import com.suppergerrie2.ai.MinecraftAI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class CommandLogin extends CommandBase {

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/login {String, username} {String, password} [bool, saveRefreshCode] ";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2 && args.length != 3) {
            //TODO: Translatable
            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
            return;
        }

        boolean saveCode = false;
        if (args.length == 3) {
            saveCode = Boolean.parseBoolean(args[2]);
        }

        try {
            MinecraftAI.chaosNetClient.Authorize(args[0], args[1], saveCode);
            sender.sendMessage(new TextComponentString("Login succeeded!!"));
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponentString("Something went wrong!"));
        }
    }
}
