package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandSetTree extends SubCommand {

	public static final String SETTREE = "settree";
	public static final String DEFAULTJOCODE = "JP";

	@Override
	public String getName() {
		return SETTREE;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		switch(args.length) {
			case 2: 
			case 3: 
			case 4: return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
			case 5: return CommandBase.getListOfStringsMatchingLastWord(args, Species.REGISTRY.getKeys());
			case 6: return Lists.newArrayList(DEFAULTJOCODE);
		}

		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		BlockPos pos = BlockPos.ORIGIN;
		Species species = null;
		String joCode = "";

		if(args.length < 5) {
			throw new WrongUsageException("commands.dynamictrees.setree.usage", new Object[0]);
		}

		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 3: pos = CommandBase.parseBlockPos(sender, args, 1, false); break;
				case 4: 
					species = TreeRegistry.findSpeciesSloppy(args[4]);
					if(species == Species.NULLSPECIES) {
						throw new WrongUsageException("commands.dynamictrees.setree.specieserror", args[4]);
					}
					break;
				case 5:	joCode = args[5]; break;
			}
		}


		if(joCode.isEmpty()) {
			joCode = DEFAULTJOCODE;
		}

		species.getJoCode(joCode).setCareful(true).generate(world, species, pos, world.getBiome(pos), EnumFacing.SOUTH, 8, SafeChunkBounds.ANY);
	}

}
