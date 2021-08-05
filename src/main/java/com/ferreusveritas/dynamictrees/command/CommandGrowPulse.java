package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CommandGrowPulse extends SubCommand {

	public static final String GROWPULSE = "growpulse";

	@Override
	public String getName() {
		return GROWPULSE;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		switch (args.length) {
			case 2:
			case 3:
			case 4:
				return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
		}

		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {

		if (args.length < 4) {
			throw new WrongUsageException("commands.dynamictrees.growpulse.usage");
		}

		BlockPos pos = CommandBase.parseBlockPos(sender, args, 1, false);

		ITreePart part = TreeHelper.getTreePart(world.getBlockState(pos));
		if (part.isRootNode()) {
			TreeHelper.growPulse(world, pos);
		} else {
			throw new CommandException("commands.dynamictrees.growpulse.norootyerror", pos.getX() + " " + pos.getY() + " " + pos.getZ());
		}
	}

}
