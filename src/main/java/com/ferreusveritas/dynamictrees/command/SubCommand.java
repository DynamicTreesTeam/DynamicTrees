package com.ferreusveritas.dynamictrees.command;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SubCommand {
	
	public abstract String getName();
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return Collections.emptyList();
	}
	
	public abstract void execute(World world, ICommandSender sender, String[] args) throws CommandException;
	
}
