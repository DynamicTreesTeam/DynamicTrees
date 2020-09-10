package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
        LiteralArgumentBuilder<CommandSource> subCommand = Commands.literal(this.getName()).executes(this::execute);

        if (this.takesCoordinates) subCommand = subCommand.then(Commands.argument("location", Vec3Argument.vec3())
                .executes(context -> this.executeWithCoords(context, context.getSource().getWorld(), Vec3Argument.getLocation(context, "location").getBlockPos(context.getSource()))));

        return subCommand;
    }

    protected void sendMessage (CommandContext<CommandSource> context, ITextComponent message) {
        context.getSource().sendFeedback(message, true);
    }

}
