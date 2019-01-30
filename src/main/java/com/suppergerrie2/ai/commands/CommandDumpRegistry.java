package com.suppergerrie2.ai.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class CommandDumpRegistry extends CommandBase {
    @Override
    public String getName() {
        return "dumpregistry";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/dumpregistry {filename}";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length != 1 || args[0].length() < 1) {
            throw new CommandException("Please pass in a filename");
        }

        JsonArray blocks = new JsonArray();
        for (Block block : Block.REGISTRY) {
            blocks.add(block.getRegistryName().toString());
        }

        JsonArray recipes = new JsonArray();
        for (IRecipe recipe : CraftingManager.REGISTRY) {

            recipes.add(recipe.getRegistryName().toString());
        }

        JsonArray items = new JsonArray();
        for (Item item : Item.REGISTRY) {
            items.add(item.getRegistryName().toString());
        }

        JsonObject dump = new JsonObject();

        dump.add("items", items);
        dump.add("blocks", blocks);

        dump.add("recipes", recipes);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(dump);

        File file = new File(args[0]);

        try {
            if (!file.exists() || !file.isFile()) {
                if(!file.createNewFile()) {
                    throw new CommandException("Couldnt create file");
                }
            }

            Files.write(file.toPath(), Collections.singleton(jsonString));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
