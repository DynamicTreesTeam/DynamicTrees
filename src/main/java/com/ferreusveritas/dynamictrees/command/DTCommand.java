package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class DTCommand extends CommandBase {
	
	public static final String COMMAND = "dt";
	public static final String REPOP = "repop";
	public static final String SETTREE = "settree";	
	
	public static final String DEFAULTJOCODE = "JP";
	
	@Override
	public String getName() {
		return COMMAND;
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
        return "commands.dynamictrees.usage";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if(args.length == 0) {
			throw new WrongUsageException("commands.dynamictrees.usage");
		}
		
		if(args.length >= 1) {
			if(REPOP.equals(args[0])) {
				sender.sendMessage(new TextComponentTranslation("commands.dynamictrees.repop.run"));
				WorldGenRegistry.populateDataBase();
			}
			else
			if(SETTREE.equals(args[0])) {
				BlockPos pos = BlockPos.ORIGIN;
				Species species = null;
				String joCode = "";
				
				if(args.length < 5) {
					throw new WrongUsageException("commands.dynamictrees.setree.usage", new Object[0]);
				}
				
				switch(args.length) {
					case 6:	joCode = args[5];
					case 5: species = TreeRegistry.findSpeciesSloppy(args[4]);
					case 4: pos = parseBlockPos(sender, args, 1, false);
				}
				
				if(species == Species.NULLSPECIES) {
					throw new WrongUsageException("commands.dynamictrees.setree.specieserror", args[4]);
				}
				
				if(joCode.isEmpty()) {
					joCode = DEFAULTJOCODE;
				}
				
				World world = sender.getEntityWorld();
	            				
				species.getJoCode(joCode).setCareful(true).generate(world, species, pos, world.getBiome(pos), EnumFacing.SOUTH, 8, SafeChunkBounds.ANY);
			}
			else {
				throw new WrongUsageException("commands.dynamictrees.usage");
			}
		}
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
        if ( args.length == 1 ) {
            return getListOfStringsMatchingLastWord(args, REPOP, SETTREE);
        }
        
        if( args.length >= 2 ) {
        	if(SETTREE.equals(args[0])) {
        		switch(args.length) {
        			case 2: 
        			case 3: 
        			case 4: return getTabCompletionCoordinate(args, 1, targetPos);
        			case 5: return getListOfStringsMatchingLastWord(args, Species.REGISTRY.getKeys());
        			case 6: return Lists.newArrayList(DEFAULTJOCODE);
        		}
        	}
        }
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}
	
}
