package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;

public final class RotateJoCodeCommand extends SubCommand {

    public RotateJoCodeCommand() {
        this.defaultToExecute = false;

        this.extraArguments = Commands.argument(CommandConstants.JO_CODE_ARGUMENT, StringArgumentType.string()).suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("JP"), builder))
                .then(Commands.argument(CommandConstants.TURNS_ARGUMENT, IntegerArgumentType.integer()).executes(this::execute));
    }

    @Override
    protected String getName() {
        return CommandConstants.ROTATE_JO_CODE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final int turns = (3 - (IntegerArgumentType.getInteger(context, CommandConstants.TURNS_ARGUMENT) % 4)) + 3;
        final String code = new JoCode(StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT)).rotate(Direction.byHorizontalIndex(turns)).toString();

        this.sendMessage(context, new StringTextComponent(code));

        return 0;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

}
