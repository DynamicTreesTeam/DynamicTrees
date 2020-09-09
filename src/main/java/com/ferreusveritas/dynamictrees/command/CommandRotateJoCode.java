package com.ferreusveritas.dynamictrees.command;

import java.util.List;

import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CommandRotateJoCode extends SubCommand {

	public static final String ROTATEJOCODE = "rotatejocode";

	@Override
	public String getName() {
		return ROTATEJOCODE;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		switch(args.length) {
			case 2: return Lists.newArrayList(CommandSetTree.DEFAULTJOCODE);
			case 3: return Lists.newArrayList("0"); 
		}

		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		
		String code = "";
		int turns = 0;
		
		if(args.length < 2) {
			throw new WrongUsageException("commands.dynamictrees.rotatejocode.usage", new Object[0]);
		}

		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 1: code = args[1]; break;
				case 2:
					try {
						turns = Integer.parseInt(args[2]);
					} catch(NumberFormatException e) {
						throw new WrongUsageException("commands.dynamictrees.rotatejocode.turnserror", args[2]);
					}
					break;
			}
		}

		if(code.isEmpty()) {
			code = CommandSetTree.DEFAULTJOCODE;
		}
		
		turns = (3 - (turns % 4)) + 3;
		code = new JoCode(code).rotate(EnumFacing.getHorizontal(turns)).toString();
		sender.sendMessage((new TextComponentString(code)));
	}

}
