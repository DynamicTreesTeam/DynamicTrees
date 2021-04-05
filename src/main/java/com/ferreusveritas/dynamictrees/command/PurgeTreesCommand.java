package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class PurgeTreesCommand extends ChunkBasedCommand {

    @Override
    protected void processChunk(World world, ChunkPos chunkPos, int radius) {
        ChunkTreeHelper.removeAllBranchesFromChunk(world, chunkPos, radius);
    }

    @Override
    protected String getName() {
        return CommandConstants.PURGE_TREES;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
