package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeTransform;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class CommandTransform extends SubCommand {

	public static final String TRANSFORM = "transform";

	@Override
	public String getName() {
		return TRANSFORM;
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
		BlockPos pos = BlockPos.ORIGIN;
		Species toSpecies = null;

		if(args.length < 5) {
			throw new WrongUsageException("commands.dynamictrees.transform.usage");
		}

		for(int arg = 0; arg < args.length; arg++) {
			switch(arg) {
				case 3: pos = CommandBase.parseBlockPos(sender, args, 1, false); break;
				case 4:
					toSpecies = TreeRegistry.findSpeciesSloppy(args[4]);
					if(toSpecies == Species.NULLSPECIES) {
						throw new CommandException("commands.dynamictrees.setree.specieserror", args[4]);
					}
					break;
			}
		}
		
		BlockPos rootPos = TreeHelper.findRootNode(world, pos);
		Species fromSpecies = TreeHelper.getExactSpecies(world, rootPos);
		
		if (rootPos == BlockPos.ORIGIN) {
			throw new CommandException("commands.dynamictrees.soillife.notreeerror", pos.getX() + " " + pos.getY() + " " + pos.getZ());
		}

		if (!toSpecies.isTransformable() || !fromSpecies.isTransformable()) {
			throw new CommandException("commands.dynamictrees.transform.nottransformableerror", !toSpecies.isTransformable() ? args[4] : fromSpecies.getRegistryName());
		}

		IBlockState rootyState = world.getBlockState(rootPos);
		BlockRooty rootyBlock = TreeHelper.getRooty(rootyState);
		
		rootyBlock.startAnalysis(world, rootPos, new MapSignal(new NodeTransform(fromSpecies, toSpecies)));

		if (rootyBlock.getSpecies(rootyState, world, rootPos) != toSpecies) {
			// Place new rooty dirt block in case we're transforming to species that requires tile entity.
			toSpecies.placeRootyDirtBlock(world, rootPos, rootyBlock.getSoilLife(rootyState, world, rootPos));
		}
	}
	
}
