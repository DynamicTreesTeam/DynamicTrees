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
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public final class RotateJoCodeCommand extends SubCommand {

    public RotateJoCodeCommand() {
        this.extraArguments = Commands.argument(CommandConstants.JO_CODE_ARGUMENT, StringArgumentType.string()).suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("JP"), builder))
                .then(Commands.argument(CommandConstants.TURNS_ARGUMENT, IntegerArgumentType.integer()).executes(context -> this.rotateJoCode(context, StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT), IntegerArgumentType.getInteger(context, CommandConstants.TURNS_ARGUMENT))));
    }

    @Override
    protected String getName() {
        return CommandConstants.ROTATE_JO_CODE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.rotatejocode.failure"));
        return 0;
    }

    private int rotateJoCode (CommandContext<CommandSource> context, final String joCode, int turns) {
        turns = (3 - (turns % 4)) + 3;

        String code = new JoCode(joCode).rotate(Direction.byHorizontalIndex(turns)).toString();
        this.sendMessage(context, new StringTextComponent(code));

        return 1;
    }

}
