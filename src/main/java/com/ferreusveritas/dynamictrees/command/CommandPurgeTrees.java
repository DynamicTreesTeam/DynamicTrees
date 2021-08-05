package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CommandPurgeTrees extends CommandChunkBased {

	public static final String PURGETREES = "purgeTrees";

	@Override
	public String getName() {
		return PURGETREES;
	}

	@Override
	void processChunk(World world, ChunkPos cPos, int radius) {
		ChunkTreeHelper.removeAllBranchesFromChunk(world, cPos, radius);
	}

}
