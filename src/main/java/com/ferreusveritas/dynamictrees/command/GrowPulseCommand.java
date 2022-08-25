package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;

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
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), 1)))
                .then(Commands.argument(NUMBER, IntegerArgumentType.integer(1)).suggests(((context, builder) -> SharedSuggestionProvider.suggest(NUMBER_SUGGESTIONS, builder)))
                        .executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), intArgument(context, NUMBER)))));
    }

    private void sendGrowPulse(final CommandSourceStack source, final BlockPos rootPos, final int number) {
        for (int i = 0; i < number; i++) {
            TreeHelper.growPulse(source.getLevel(), rootPos);
        }

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.grow_pulse",
                CommandHelper.colour(String.valueOf(number), ChatFormatting.AQUA),
                CommandHelper.posComponent(rootPos, ChatFormatting.AQUA)));
    }

}
