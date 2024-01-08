package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class GetRootsCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.GET_ROOTS;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String CODE_RAW = "code_raw";

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().executes(context -> this.getRoots(context.getSource(), blockPosArgument(context), false))
                .then(booleanArgument(CODE_RAW).executes(context -> this.getRoots(context.getSource(), blockPosArgument(context),
                        booleanArgument(context, CODE_RAW))));
    }

    private int getRoots(final CommandSourceStack source, final BlockPos pos, final boolean codeRaw) {
        final Level level = source.getLevel();

        return TreeHelper.getBestGuessSpecies(level, pos).ifValidElse(species -> {
                    final Optional<JoCode> joCode = TreeHelper.getRootsJoCode(level, pos);

                    if (codeRaw) {
                        sendSuccess(source, Component.literal(joCode.map(JoCode::toString).orElse("?")));
                    } else {
                        sendSuccess(source, Component.translatable("commands.dynamictrees.success.get_roots",
                                species.getTextComponent(), joCode.map(JoCode::getTextComponent)
                                .orElse(Component.literal("?"))));
                    }
                }, () -> sendFailure(source, Component.translatable("commands.dynamictrees.error.get_tree",
                        CommandHelper.posComponent(pos).copy().withStyle(style -> style.withColor(ChatFormatting.DARK_RED))))
        ) ? 1 : 0;
    }

}
