package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class ClearOrphanedCommand extends ChunkBasedCommand {

    @Override
    protected void processChunk(World world, ChunkPos chunkPos, int radius) {
        ChunkTreeHelper.removeOrphanedBranchNodes(world, chunkPos, radius);
    }

    @Override
    protected String getName() {
        return CommandConstants.CLEAR_ORPHANED;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

}
