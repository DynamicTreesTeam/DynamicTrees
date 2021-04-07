package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.util.ChunkTreeHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class ClearOrphanedCommand extends ChunkBasedCommand {

    @Override
    protected String getName() {
        return CommandConstants.CLEAR_ORPHANED;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    protected void processChunk(CommandSource source, World world, ChunkPos chunkPos, int radius) {
        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.clear_orphaned",
                aqua(ChunkTreeHelper.removeOrphanedBranchNodes(world, chunkPos, radius))), true);
    }

}
