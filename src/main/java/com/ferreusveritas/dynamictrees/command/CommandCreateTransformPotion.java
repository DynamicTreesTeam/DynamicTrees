package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class CommandCreateTransformPotion extends SubCommand {

	public static final String CREATE_TRANSFORM_POTION = "createtransformpotion";

	@Override
	public String getName() {
		return CREATE_TRANSFORM_POTION;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {

		switch(args.length) {
			case 2:
			case 3:
			case 4: return CommandBase.getTabCompletionCoordinate(args, 1, targetPos);
			case 5: return CommandBase.getListOfStringsMatchingLastWord(args, TreeRegistry.getTransformableSpeciesLocs());
		}
		
		return super.getTabCompletions(server, sender, args, targetPos);
	}

	@Override
	public void execute(World world, ICommandSender sender, String[] args) throws CommandException {
		if(args.length < 4) {
			throw new WrongUsageException("commands.dynamictrees.createtransformpotion.usage");
		}

		BlockPos pos = BlockPos.ORIGIN;
		Species species = null;

		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 3: pos = CommandBase.parseBlockPos(sender, args, 1, false); break;
				case 4:
					species = TreeRegistry.findSpeciesSloppy(args[4]);
					if(species == Species.NULLSPECIES) {
						throw new CommandException("commands.dynamictrees.setree.specieserror", args[4]);
					}
					break;
			}
		}

		final DendroPotion dendroPotion = ModItems.dendroPotion;
		final ItemStack dendroPotionStack = new ItemStack(dendroPotion, 1, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Create the transform potion.
		dendroPotion.setTargetSpecies(dendroPotionStack, species); // Tell it to set the target species to the given species.

		ItemUtils.spawnItemStack(world, pos, dendroPotionStack, true); // Spawn potion in the world.
	}
	
}
