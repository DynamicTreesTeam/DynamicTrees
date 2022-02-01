package com.ferreusveritas.dynamictrees.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public abstract class CommandChunkBased extends SubCommand {
	
	private final int chunkMax = 1875000;
	
	@Override
	public abstract String getName();
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
		switch (args.length) {
			case 2:
			case 3:
				return getTabCompletionCoordinate(args, 1, targetPos);
			case 4:
				return Lists.newArrayList(Integer.toString(0));
		}
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}
	
	public abstract String messageToThrow();
	
	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		
		if (!(args.length == 3 || args.length == 4)) {
			throw new WrongUsageException(messageToThrow());
		}
		
		BlockPos blockpos = sender.getPosition();
		ChunkPos cPos = new ChunkPos(
			(int) CommandBase.parseDouble(blockpos.getX() >> 4, args[1], -chunkMax, chunkMax, false),
			(int) CommandBase.parseDouble(blockpos.getZ() >> 4, args[2], -chunkMax, chunkMax, false)
		);
		
		int radius = args.length == 4 ? CommandBase.parseInt(args[3]) : 0;
		
		processChunk(world, cPos, radius);
	}
	
	public static List<String> getTabCompletionCoordinate(String[] inputArgs, int index, @Nullable BlockPos pos) {
		if (pos == null) {
			return Lists.newArrayList("~");
		}
		
		int v;
		switch (inputArgs.length - index - 1) {
			case 0:
				v = pos.getX();
				break;
			case 1:
				v = pos.getZ();
				break;
			default:
				return Collections.emptyList();
		}
		
		return Lists.newArrayList(Integer.toString(v >> 4));
	}
	
	
	abstract void processChunk(World world, ChunkPos cPos, int radius);
	
}
