package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public abstract class ChunkBasedCommand extends SubCommand {

    public ChunkBasedCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;
        this.extraArguments = Commands.argument(CommandConstants.RADIUS_ARGUMENT, IntegerArgumentType.integer(0))
                .executes(this::execute);
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final ChunkPos chunkPos = new ChunkPos(Vec3Argument.getCoordinates(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource()));
        final int radius = IntegerArgumentType.getInteger(context, CommandConstants.RADIUS_ARGUMENT);

        this.processChunk(context.getSource().getLevel(), chunkPos, radius);
        return 1;
    }

    protected abstract void processChunk(World world, ChunkPos chunkPos, int radius);

}
