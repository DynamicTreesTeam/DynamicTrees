package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class CommandSoilLife extends SubCommand {

	public static final String SOILLIFE = "soillife";

	@Override
	public String getName() {
		return SOILLIFE;
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
			throw new WrongUsageException("commands.dynamictrees.soillife.usage");
		}

		BlockPos pos = CommandBase.parseBlockPos(sender, args, 1, false);

		BlockPos rootPos = TreeHelper.findRootNode(world, pos);
		if (rootPos != BlockPos.ORIGIN) {
			IBlockState state = world.getBlockState(rootPos);

			if (args.length >= 5) {
				int life = 0;
				try {
					life = Integer.parseInt(args[4]);
				} catch (NumberFormatException e) {
					throw new CommandException("commands.dynamictrees.soillife.lifeerror", args[4]);
				}

				TreeHelper.getRooty(state).setSoilLife(world, rootPos, life);
			} else {
				int life = TreeHelper.getRooty(state).getSoilLife(state, world, rootPos);
				sender.sendMessage((new TextComponentString("" + life)));
			}
		} else {
			throw new CommandException("commands.dynamictrees.soillife.notreeerror", pos.getX() + " " + pos.getY() + " " + pos.getZ());
		}

	}

}
