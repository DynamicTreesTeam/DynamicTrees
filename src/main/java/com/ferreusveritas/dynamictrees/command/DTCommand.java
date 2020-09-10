package com.ferreusveritas.dynamictrees.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DTCommand extends CommandBase {

	public static final String COMMAND = "dt";

	private Map<String, SubCommand> commands = new HashMap<>();

	private void addSubCommand(SubCommand command) {
		commands.put(command.getName(), command);
	}

	public DTCommand() {
		addSubCommand(new CommandRepop());
		addSubCommand(new CommandSetTree());
		addSubCommand(new CommandGetTree());
		addSubCommand(new CommandGrowPulse());
		addSubCommand(new CommandKillTree());
		addSubCommand(new CommandSoilLife());
		addSubCommand(new CommandSpeciesList());
		addSubCommand(new CommandCreateStaff());
		addSubCommand(new CommandRotateJoCode());
		addSubCommand(new CommandSetCoordXor());
	}

	@Override
	public String getName() {
		return COMMAND;
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.dynamictrees.usage";
	}

	private String getSubCommands() {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(String name : commands.keySet()) {
			if(!first) {
				builder.append('|');
			}
			builder.append(name);
			first = false;
		}
		return builder.toString();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if(args.length == 0) {
			throw new WrongUsageException("commands.dynamictrees.usage", getSubCommands());
		}

		String subCommand = args[0];
		World world = sender.getEntityWorld();

		if(args.length >= 1) {
			if(commands.containsKey(subCommand)) {
				commands.get(subCommand).execute(world, sender, args);
			}	
			else {
				throw new WrongUsageException("commands.dynamictrees.usage", getSubCommands());
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		if ( args.length == 1 ) {
			return getListOfStringsMatchingLastWord(args, commands.keySet());
		}

		String subCommand = args[0];

		if( args.length >= 2 ) {
			if(commands.containsKey(subCommand)) {
				return commands.get(subCommand).getTabCompletions(server, sender, args, targetPos);
			}
		}

		return super.getTabCompletions(server, sender, args, targetPos);
	}

}
