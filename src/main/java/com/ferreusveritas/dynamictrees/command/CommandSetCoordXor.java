package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandSetCoordXor extends SubCommand {
	
	public static final String SETCOORDXOR = "setcoordxor";
	
	@Override
	public String getName() {
		return SETCOORDXOR;
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
		switch(args.length) {
			case 2: return Lists.newArrayList("0");
		}
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}
	
	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		int xor = 0;
		
		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 1: {
					String work = args[1];
					try {
						xor = Integer.parseInt(work);
					} catch(NumberFormatException e) {
						xor = work.hashCode();
					}
				}
			}
		}
		
		CoordUtils.coordXor = xor;
	}

}
