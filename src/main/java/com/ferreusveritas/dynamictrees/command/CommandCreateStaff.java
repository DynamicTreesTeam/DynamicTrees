package com.ferreusveritas.dynamictrees.command;

import java.awt.Color;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandUtils;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scala.actors.threadpool.Arrays;

public class CommandCreateStaff extends SubCommand {

	public static final String CREATESTAFF = "createstaff";
	public static final String DEFAULTJOCODE = "JP";
	
	@Override
	public String getName() {
		return CREATESTAFF;
	}

	public List<String> getJoCode(World world, BlockPos targetPos) {
		return Arrays.asList(new String[] { TreeHelper.getJoCode(world, targetPos).map(JoCode::toString).orElse(DEFAULTJOCODE) });
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		
		switch(args.length) {
			case 2: 
			case 3: 
			case 4: return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
			case 5: return CommandBase.getListOfStringsMatchingLastWord(args, Species.REGISTRY.getKeys());
			case 6: return getJoCode(sender.getEntityWorld(), targetPos);
			case 7: return Arrays.asList(new Object[] {"#00FFFF"});
			case 8: return CommandBase.getListOfStringsMatchingLastWord(args, new String[] {"true", "false"});
			case 9: return Arrays.asList(new String[] {"64"});
		}
		
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		
		if(args.length < 4) {
			throw new WrongUsageException("commands.dynamictrees.createstaff.usage", new Object[0]);
		}
		
		BlockPos pos = CommandBase.parseBlockPos(sender, args, 1, false);
		
		ItemStack stack = new ItemStack(ModItems.treeStaff, 1, 0);
		ModItems.treeStaff.setSpecies(stack, TreeRegistry.findSpeciesSloppy("oak"));
		
		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 4: Species species = TreeRegistry.findSpeciesSloppy(args[4]);
						if(species == Species.NULLSPECIES) {
							throw new CommandException("commands.dynamictrees.createstaff.specieserror", args[4]);
						}
						ModItems.treeStaff.setSpecies(stack, species);
						break;
				case 5: ModItems.treeStaff.setCode(stack, args[5]);
						break;
				case 6:	try {
							Color.decode(args[6]).getRGB();
							ModItems.treeStaff.setColor(stack, args[6]);
						} catch (NumberFormatException e) {
							throw new CommandException("commands.dynamictrees.createstaff.colorerror", args[6]);
						}
						break;
				case 7: if("true".equals(args[7]) || "false".equals(args[7])) {
							boolean readonly = "true".equals(args[7]);
							ModItems.treeStaff.setReadOnly(stack, readonly);
						} else {
							throw new CommandException("commands.dynamictrees.createstaff.readonlyerror", args[7]);
						}
						break;
				case 8: try { 
							int maxUses = Integer.decode(args[8]);
							if(maxUses <= 0) {
								throw new CommandException("commands.dynamictrees.createstaff.maxuseserror", args[8]);
							}
							ModItems.treeStaff.setMaxUses(stack, maxUses);
							ModItems.treeStaff.setUses(stack, maxUses);
						} catch(NumberFormatException e) {
							throw new CommandException("commands.dynamictrees.createstaff.maxuseserror", args[8]);
						}
			}
		}

		CommandUtils.spawnItemStack(world, pos, stack);
	}

}
