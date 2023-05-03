package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

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
    protected void processChunk(CommandSourceStack source, Level level, ChunkPos chunkPos, int radius) {
        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.purge_trees",
                aqua(ChunkTreeHelper.removeAllBranchesFromChunk(level, chunkPos, radius))));
    }

}
