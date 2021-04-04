package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public abstract class SubCommand {

    /**
     * takesCoordinates - Set this to true in constructor in order to add location arguments to the command.
     * Handle the execution with coordinates by overriding executeWithCoords() method.
     */
    protected boolean takesCoordinates = false;

    /**
     * executesWithCoordinates - Set this to false to disable calling execute when coordinate argument is given only.
     */
    protected boolean executesWithCoordinates = true;

    /**
     * defaultToExecute - Set this to false to disable calling execute() when no arguments are given.
     */
    protected boolean defaultToExecute = true;

    /**
     * extraArguments - Append any extra arguments you wish to add onto the command in the constructor.
     * These will be registered automatically.
     */
    protected RequiredArgumentBuilder<CommandSource, ?> extraArguments = null;

    /**
     * Returns the name of the command.
     *
     * @return - Name of command.
     */
    protected abstract String getName ();

    /**
     * Returns the permission level required to use the command.
     *
     * @return Permission level required.
     */
    protected abstract int getPermissionLevel ();

    /**
     * Call this method on valid command execution (when all arguments are given).
     * Implement it to include command logic.
     *
     * @param context Context of the command.
     * @return Integer value which is returned to ArgumentBuilder.executes.
     */
    protected abstract int execute (CommandContext<CommandSource> context);

    public ArgumentBuilder<CommandSource, ?> register() {
        LiteralArgumentBuilder<CommandSource> subCommandBuilder = Commands.literal(this.getName());

        subCommandBuilder.requires(commandSource -> commandSource.hasPermission(this.getPermissionLevel()));

        if (this.defaultToExecute)
            subCommandBuilder.executes(this::execute);

        ArgumentBuilder<CommandSource, ?> subSubCommandBuilder = null;

        if (this.takesCoordinates) {
            subSubCommandBuilder = Commands.argument(CommandConstants.LOCATION_ARGUMENT, Vec3Argument.vec3());

            if (this.executesWithCoordinates)
                subSubCommandBuilder.executes(this::execute);
        }

        if (this.extraArguments != null) {
            if (subSubCommandBuilder == null)
                subSubCommandBuilder = this.extraArguments;
            else subSubCommandBuilder.then(this.extraArguments);
        }

        if (subSubCommandBuilder == null)
            return subCommandBuilder;

        return subCommandBuilder.then(subSubCommandBuilder);
    }

    /**
     * Sends a message to the command sender.
     *
     * @param context The command context.
     * @param message The message to send.
     */
    protected void sendMessage (CommandContext<CommandSource> context, ITextComponent message) {
        context.getSource().sendSuccess(message, true);
    }

    protected BlockPos getPositionArg (CommandContext<CommandSource> context) {
        return Vec3Argument.getCoordinates(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
    }

    protected Species getSpeciesArg (CommandContext<CommandSource> context) {
        return TreeRegistry.findSpecies(ResourceLocationArgument.getId(context, CommandConstants.SPECIES_ARGUMENT));
    }

    protected BlockPos getRootPos (CommandContext<CommandSource> context) {
        return this.getRootPos(context, context.getSource().getLevel());
    }

    protected BlockPos getRootPos (CommandContext<CommandSource> context, World world) {
        return this.getRootPos(context, world, this.getPositionArg(context));
    }

    protected BlockPos getRootPos (CommandContext<CommandSource> context, World world, BlockPos pos) {
        return TreeHelper.findRootNode(world, pos);
    }

}
