package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GrowPulseCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.GROW_PULSE;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final String NUMBER = "number";
    private static final Collection<String> NUMBER_SUGGESTIONS = Stream.of(1, 4, 8, 16, 32, 64).map(String::valueOf).collect(Collectors.toList());

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), 1)))
                .then(Commands.argument(NUMBER, IntegerArgumentType.integer(1)).suggests(((context, builder) -> ISuggestionProvider.suggest(NUMBER_SUGGESTIONS, builder)))
                        .executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), intArgument(context, NUMBER)))));
    }

    private void sendGrowPulse(final CommandSource source, final BlockPos rootPos, final int number) {
        // TODO: Make a custom packet so we can display grow pulse particles on client.

        for (int i = 0; i < number; i++)
            TreeHelper.growPulse(source.getLevel(), rootPos);

        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.grow_pulse",
                CommandHelper.colour(String.valueOf(number), TextFormatting.AQUA),
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA)), true);
    }

}
