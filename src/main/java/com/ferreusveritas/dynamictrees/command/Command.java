package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class Command extends CommandBase {
	
	@Override
	public String getName() {
		return "dt";
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "dt";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length >= 1) {
			if("repop".equals(args[0])) {
				System.out.println("Repopulating worldgen database");
				WorldGenRegistry.populateDataBase();
			}
		}
	}
	
}
