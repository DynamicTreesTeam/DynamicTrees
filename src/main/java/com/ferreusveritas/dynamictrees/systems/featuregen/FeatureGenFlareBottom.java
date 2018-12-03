package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenFlareBottom implements IPostGenFeature, IPostGrowFeature{
	
	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(soilLife > 0) {
			flareBottom(world, rootPos, species);
			return true;
		}
		return false;
	}

	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		flareBottom(world, rootPos, species);
		return true;
	}
	
	/**
	 * Put a cute little flare on the bottom of the dark oaks
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block of the tree
	 * @return The radius of the bottom trunk section after operation
	 */
	public void flareBottom(World world, BlockPos rootPos, Species species) {
		TreeFamily family = species.getFamily();
		
		//Put a cute little flare on the bottom of the dark oaks
		int radius3 = TreeHelper.getRadius(world, rootPos.up(3));
		
		if(radius3 > 6) {
			family.getDynamicBranch().setRadius(world, rootPos.up(2), radius3 + 1, EnumFacing.UP);
			family.getDynamicBranch().setRadius(world, rootPos.up(1), radius3 + 2, EnumFacing.UP);
		}
	}
	
}
