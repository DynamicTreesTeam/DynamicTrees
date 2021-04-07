package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class PurgeTreesCommand extends ChunkBasedCommand {

    @Override
    protected String getName() {
        return CommandConstants.PURGE_TREES;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    protected void processChunk(CommandSource source, World world, ChunkPos chunkPos, int radius) {
        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.purge_trees",
                aqua(ChunkTreeHelper.removeAllBranchesFromChunk(world, chunkPos, radius))), true);
    }

}
