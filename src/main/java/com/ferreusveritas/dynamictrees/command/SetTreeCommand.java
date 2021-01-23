package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public final class SetTreeCommand extends SubCommand {

    public SetTreeCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        // Register extra arguments.
        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.resourceLocation()).suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getKeys(), builder))
                .then(Commands.argument(CommandConstants.JO_CODE_ARGUMENT, StringArgumentType.string()).suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("JP"), builder))
                        .then(Commands.argument(CommandConstants.TURNS_ARGUMENT, IntegerArgumentType.integer())
                                .executes(this::execute)));
    }

    @Override
    protected String getName() {
        return CommandConstants.SET_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final Species species = TreeRegistry.findSpecies(ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT));
        final String joCode = StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT);
        final int turns = IntegerArgumentType.getInteger(context, CommandConstants.TURNS_ARGUMENT);
        final BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());

        if (species == Species.NULLSPECIES) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT)));
            return 0;
        }

        species.getJoCode(joCode).rotate(Direction.byHorizontalIndex(turns)).setCareful(true).generate(context.getSource().getWorld(), species, pos.offset(Direction.DOWN), context.getSource().getWorld().getBiome(pos), Direction.SOUTH, 8, SafeChunkBounds.ANY);
        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
