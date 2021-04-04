package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Arrays;
import java.util.Collections;

public final class SetTreeCommand extends SubCommand {

    public SetTreeCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        // Register extra arguments.
        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.resourceLocation()).suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getRegistryNames(), builder))
                .then(Commands.argument(CommandConstants.JO_CODE_ARGUMENT, StringArgumentType.string()).suggests((context, builder) -> ISuggestionProvider.suggest(Collections.singletonList("JP"), builder))
                        .then(Commands.argument(CommandConstants.TURNS_ARGUMENT, IntegerArgumentType.integer())
                                .executes(this::execute)));
    }

    @Override
    protected String getName() {
        return CommandConstants.SET_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final Species species = this.getSpeciesArg(context);
        final String joCode = StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT);
        final int turns = IntegerArgumentType.getInteger(context, CommandConstants.TURNS_ARGUMENT);
        final BlockPos pos = this.getPositionArg(context);

        if (!species.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT)));
            return 0;
        }

        ServerWorld world = context.getSource().getWorld();
        species.getJoCode(joCode).rotate(Direction.byHorizontalIndex(turns)).setCareful(true).generate(world, world, species, pos.offset(Direction.DOWN), context.getSource().getWorld().getBiome(pos), Direction.SOUTH, 8, SafeChunkBounds.ANY);
        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
