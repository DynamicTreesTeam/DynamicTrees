package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class GroupGenFeature implements IPostGenFeature {
	
	private List<IPostGenFeature> features = new ArrayList<>();
	
	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		for(IPostGenFeature feature : features) {
			feature.postGeneration(world, rootPos, species, biome, radius, endPoints, safeBounds, initialDirtState);
		}
		return true;
	}
	
	public GroupGenFeature add(IPostGenFeature feature) {
		features.add(feature);
		return this;
	}
	
}
