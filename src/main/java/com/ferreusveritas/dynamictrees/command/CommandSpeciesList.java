package com.ferreusveritas.dynamictrees.command;

import java.util.Collections;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandSpeciesList extends SubCommand {

	public static final String SPECIESLIST = "specieslist"; 
	
	@Override
	public String getName() {
		return SPECIESLIST;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        return Collections.<String>emptyList();
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		TreeRegistry.getSpeciesDirectory().forEach(r -> sender.sendMessage((new TextComponentString(r.toString()))));
	}

}
