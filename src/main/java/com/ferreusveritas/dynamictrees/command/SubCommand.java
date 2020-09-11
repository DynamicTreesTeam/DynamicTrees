package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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
     * extraArguments - Append any extra arguments you wish to add onto the command in the constructor.
     * These will be registered automatically.
     */
    protected RequiredArgumentBuilder<CommandSource, ?> extraArguments = null;

    protected abstract String getName ();
    protected abstract int execute (CommandContext<CommandSource> context);

    /**
     * Use this to perform actions when the sub command is called with appropriate coordinate arguments.
     *
     * @param context - Command context.
     * @param worldIn - World target block is in.
     * @param blockPos - Position of target block.
     * @return Integer value which is used by executes() method in ArgumentBuilder.
     */
    protected int executeWithCoords (CommandContext<CommandSource> context, World worldIn, BlockPos blockPos) {
        return 0;
    }

    public ArgumentBuilder<CommandSource, ?> register() {
        LiteralArgumentBuilder<CommandSource> subCommandBuilder = Commands.literal(this.getName()).executes(this::execute);

        ArgumentBuilder<CommandSource, ?> subSubCommandBuilder = null;

        if (this.takesCoordinates) subSubCommandBuilder = Commands.argument("location", Vec3Argument.vec3()).executes(context -> this.executeWithCoords(context, context.getSource().getWorld(), Vec3Argument.getLocation(context, "location").getBlockPos(context.getSource())));

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
