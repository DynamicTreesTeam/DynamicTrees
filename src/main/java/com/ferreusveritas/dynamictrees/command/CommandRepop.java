package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class CommandRepop extends SubCommand {

	public static final String REPOP = "repop";
	
	@Override
	public String getName() {
		return REPOP;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentTranslation("commands.dynamictrees.repop.run"));
		WorldGenRegistry.populateDataBase();
	}

}
