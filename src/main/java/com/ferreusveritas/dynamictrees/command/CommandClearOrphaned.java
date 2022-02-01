package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class CommandClearOrphaned extends CommandChunkBased {
	
	public static final String CLEARORPHANED = "clearOrphaned";
	
	@Override
	public String getName() {
		return CLEARORPHANED;
	}
	
	@Override
	public String messageToThrow() {
		return "commands.dynamictrees.clearorphaned.usage";
	}
	
	@Override
	void processChunk(World world, ChunkPos cPos, int radius) {
		ChunkTreeHelper.removeOrphanedBranchNodes(world, cPos, radius);
	}
	
}
