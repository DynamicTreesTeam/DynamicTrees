package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.compat.WailaOther;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SubstanceMega implements ISubstanceEffect {

	@Override
	public boolean apply(World world, BlockPos rootPos) {

		IBlockState blockState = world.getBlockState(rootPos);
		BlockRooty dirt = TreeHelper.getRooty(blockState);
		Species species = dirt.getSpecies(blockState, world, rootPos);
		Species megaSpecies = species.getMegaSpecies();

		if (megaSpecies.isValid()) {
			int life = dirt.getSoilLife(blockState, world, rootPos);
			megaSpecies.placeRootyDirtBlock(world, rootPos, life);

			blockState = world.getBlockState(rootPos);
			dirt = TreeHelper.getRooty(blockState);

			if (dirt.getSpecies(blockState, world, rootPos) == megaSpecies) {
				TreeHelper.treeParticles(world, rootPos, EnumParticleTypes.DRAGON_BREATH, 8);
				WailaOther.invalidateWailaPosition();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean update(World world, BlockPos rootPos, int deltaTicks) {
		return false;
	}

	@Override
	public String getName() {
		return "mega";
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
