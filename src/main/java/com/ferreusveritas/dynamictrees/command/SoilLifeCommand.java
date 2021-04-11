package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SoilLifeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SOIL_LIFE;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String SOIL_LIFE = "soil_life";
    private static final String RAW = "raw";

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.getSoilLife(context.getSource(),
                rootPosArgument(context), false)))
                .then(booleanArgument(RAW).executes(context -> executesSuccess(() -> this.getSoilLife(context.getSource(),
                        rootPosArgument(context), booleanArgument(context, RAW)))))
                .then(Commands.argument(SOIL_LIFE, IntegerArgumentType.integer(0, 15)).suggests(((context, builder) ->
                        ISuggestionProvider.suggest(Stream.of(0, 7, 15).map(String::valueOf).collect(Collectors.toList()), builder)))
                        .requires(commandSource -> commandSource.hasPermission(2)) // Setting soil life requires higher permission level.
                        .executes(context -> executesSuccess(() -> this.setSoilLife(context.getSource(), rootPosArgument(context),
                                intArgument(context, SOIL_LIFE)))));
    }

    private void getSoilLife(final CommandSource source, final BlockPos rootPos, final boolean raw) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        final int soilLife = Objects.requireNonNull(TreeHelper.getRooty(state)).getSoilLife(state, source.getLevel(), rootPos);

        if (raw) {
            source.sendSuccess(new StringTextComponent(String.valueOf(soilLife)), false);
            return;
        }

        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.get_soil_life",
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA),
                CommandHelper.colour(String.valueOf(soilLife), TextFormatting.AQUA)), false);
    }

    private void setSoilLife(final CommandSource source, final BlockPos rootPos, final int soilLife) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        Objects.requireNonNull(TreeHelper.getRooty(state)).setSoilLife(source.getLevel(), rootPos, soilLife);

        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.set_soil_life",
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA),
                CommandHelper.colour(String.valueOf(soilLife), TextFormatting.AQUA)), true);
    }

}
