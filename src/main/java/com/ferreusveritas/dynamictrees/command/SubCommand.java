package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.text.ITextComponent;

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
     * Use this to set the name of the command.
     *
     * @return - Name of command.
     */
    protected abstract String getName ();

    /**
     * Call this method on valid command execution (when all arguments are given).
     * Implement it to include command logic.
     *
     * @param context - Context of the command.
     * @return - Integer value which is returned to ArgumentBuilder.executes.
     */
    protected abstract int execute (CommandContext<CommandSource> context);

    public ArgumentBuilder<CommandSource, ?> register() {
        LiteralArgumentBuilder<CommandSource> subCommandBuilder = Commands.literal(this.getName());

        if (this.defaultToExecute) subCommandBuilder.executes(this::execute);

        ArgumentBuilder<CommandSource, ?> subSubCommandBuilder = null;

        if (this.takesCoordinates) {
            if (this.executesWithCoordinates) subSubCommandBuilder = Commands.argument(CommandConstants.LOCATION_ARGUMENT, Vec3Argument.vec3()).executes(this::execute);
            else subSubCommandBuilder = Commands.argument(CommandConstants.LOCATION_ARGUMENT, Vec3Argument.vec3());
        }

        if (this.extraArguments != null) {
            if (subSubCommandBuilder == null) subSubCommandBuilder = this.extraArguments;
            else subSubCommandBuilder.then(this.extraArguments);
        }

        if (subSubCommandBuilder == null) return subCommandBuilder;
        return subCommandBuilder.then(subSubCommandBuilder);
    }

    protected void sendMessage (CommandContext<CommandSource> context, ITextComponent message) {
        context.getSource().sendFeedback(message, true);
    }

}
