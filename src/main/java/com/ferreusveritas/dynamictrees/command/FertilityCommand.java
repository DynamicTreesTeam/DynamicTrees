package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Objects;

import static com.ferreusveritas.dynamictrees.command.CommandConstants.FERTILITY_SUGGESTIONS;
import static com.ferreusveritas.dynamictrees.command.CommandConstants.RAW;

public final class FertilityCommand extends SubCommand {

    @Override
    protected String getName() {
        return FERTILITY;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String FERTILITY = CommandConstants.FERTILITY;

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
                        rootPosArgument(context), false)))
                .then(booleanArgument(RAW).executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
                        rootPosArgument(context), booleanArgument(context, RAW)))))
                .then(Commands.argument(FERTILITY, IntegerArgumentType.integer(0, 15)).suggests(FERTILITY_SUGGESTIONS)
                        .requires(commandSource -> commandSource.hasPermission(2)) // Setting fertility requires higher permission level.
                        .executes(context -> executesSuccess(() -> this.setFertility(context.getSource(), rootPosArgument(context),
                                intArgument(context, FERTILITY)))));
    }

    private void getFertility(final CommandSource source, final BlockPos rootPos, final boolean raw) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        final int fertility = Objects.requireNonNull(TreeHelper.getRooty(state)).getFertility(state, source.getLevel(), rootPos);

        if (raw) {
            sendSuccess(source, new StringTextComponent(String.valueOf(fertility)));
            return;
        }

        sendSuccess(source, new TranslationTextComponent("commands.dynamictrees.success.get_fertility",
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA),
                CommandHelper.colour(String.valueOf(fertility), TextFormatting.AQUA)));
    }

    private void setFertility(final CommandSource source, final BlockPos rootPos, final int fertility) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        Objects.requireNonNull(TreeHelper.getRooty(state)).setFertility(source.getLevel(), rootPos, fertility);

        sendSuccessAndLog(source, new TranslationTextComponent("commands.dynamictrees.success.set_fertility",
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA),
                CommandHelper.colour(String.valueOf(fertility), TextFormatting.AQUA)));
    }

}
