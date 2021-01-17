package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public class ClearVolumeGenFeature implements IPreGenFeature {

	private final int height;

	public ClearVolumeGenFeature(int height) {
		this.height = height;
	}

    @Override
	public BlockPos preGeneration(World world, BlockPos rootPos, Species species, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode) {
		//Erase a volume of blocks that could potentially get in the way
		for(BlockPos pos : BlockPos.getAllInBoxMutable(rootPos.add(new Vector3i(-1,  1, -1)), rootPos.add(new Vector3i(1, height, 1)))) {
			world.removeBlock(pos, false);
		}
		return rootPos;
	}

}
