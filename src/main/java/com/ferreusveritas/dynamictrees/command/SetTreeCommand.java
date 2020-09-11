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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class SetTreeCommand extends SubCommand {

    public SetTreeCommand() {
        this.takesCoordinates = true;

        // Register extra arguments.
        // TODO: Get arguments within execute() so we aren't getting all the args in this.
        this.extraArguments = Commands.argument("species", ResourceLocationArgument.resourceLocation()).suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getKeys(), builder))
                .then(Commands.argument("joCode", StringArgumentType.string())
                .then(Commands.argument("turns", IntegerArgumentType.integer()).executes(context -> this.setTree(context, context.getSource().getWorld(), Vec3Argument.getLocation(context, "location").getBlockPos(context.getSource()), ResourceLocationArgument.getResourceLocation(context, "species"), StringArgumentType.getString(context, "joCode"), IntegerArgumentType.getInteger(context, "turns")))));
    }

    @Override
    protected String getName() {
        return CommandConstants.SET_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.settree.failure"));
        return 1;
    }

    private int setTree (CommandContext<CommandSource> context, World world, BlockPos pos, ResourceLocation speciesResourceLocation, String joCode, int turns) {
        Species species = TreeRegistry.findSpecies(speciesResourceLocation);

        // Generate tree.
        species.getJoCode(joCode).rotate(Direction.byHorizontalIndex(turns)).setCareful(true).generate(world, species, pos.offset(Direction.DOWN), world.getBiome(pos), Direction.SOUTH, 8, SafeChunkBounds.ANY);
        return 1;
    }

}
