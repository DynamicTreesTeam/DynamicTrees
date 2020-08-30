package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandGetTree extends SubCommand {

	public static final String GETTREE = "gettree";
	
	@Override
	public String getName() {
		return GETTREE;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
		switch(args.length) {
			case 2: 
			case 3: 
			case 4: return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
		}
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 4) {
			throw new WrongUsageException("commands.dynamictrees.getree.usage");
		}
		
		BlockPos pos = CommandBase.parseBlockPos(sender, args, 1, false);
		Species species = TreeHelper.getBestGuessSpecies(world, pos);
		String code = TreeHelper.getJoCode(world, pos).map(JoCode::toString).orElse("?");
		
		sender.sendMessage((new TextComponentString(species.toString())));
		sender.sendMessage((new TextComponentString(code)));
	}
		
}
