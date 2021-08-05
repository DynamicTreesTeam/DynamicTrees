package com.ferreusveritas.dynamictrees.command;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class CommandChunkBased extends SubCommand {

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

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {

		if (!(args.length == 1 || args.length == 3 || args.length == 4)) {
			throw new WrongUsageException("commands.dynamictrees.killtree.usage");
		}

		ChunkPos cPos = null;

		if (args.length >= 3) {
			BlockPos blockpos = sender.getPosition();
			cPos = new ChunkPos(
				(int) CommandBase.parseDouble(blockpos.getX() >> 4, args[1], -1875000, 1875000, false),
				(int) CommandBase.parseDouble(blockpos.getZ() >> 4, args[2], -1875000, 1875000, false)
			);
		} else if (args.length == 1) {
			if (sender instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) sender;
				cPos = new ChunkPos(player.getPosition());
			}
		}

		int radius = 0;

		if (args.length == 4) {
			radius = CommandBase.parseInt(args[3]);
		}

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
