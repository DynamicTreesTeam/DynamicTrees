package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public abstract class ChunkBasedCommand extends SubCommand {

    private static final String RADIUS = "radius";

    private static final int DEFAULT_RADIUS = 1;

    @Override
    public ArgumentBuilder<CommandSource, ?> register() {
        return super.register().executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
                context.getSource().getLevel(), this.getChunkPos(context.getSource()), DEFAULT_RADIUS)));
    }

    private ChunkPos getChunkPos(final CommandSource source) {
        return new ChunkPos(new BlockPos(source.getPosition().x, source.getPosition().y, source.getPosition().z));
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
                        context.getSource().getLevel(), new ChunkPos(blockPosArgument(context)), DEFAULT_RADIUS)))
                .then(Commands.argument(RADIUS, IntegerArgumentType.integer(1))
                        .executes(context -> executesSuccess(() -> this.processChunk(context.getSource(),
                                context.getSource().getLevel(), new ChunkPos(blockPosArgument(context)), intArgument(context, RADIUS)))));
    }

    protected abstract void processChunk(CommandSource source, World world, ChunkPos chunkPos, int radius);

}
