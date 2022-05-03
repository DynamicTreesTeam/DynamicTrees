package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class GetTreeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.GET_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String CODE_RAW = "code_raw";

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().executes(context -> this.getTree(context.getSource(), blockPosArgument(context), false))
                .then(booleanArgument(CODE_RAW).executes(context -> this.getTree(context.getSource(), blockPosArgument(context),
                        booleanArgument(context, CODE_RAW))));
    }

    private int getTree(final CommandSourceStack source, final BlockPos pos, final boolean codeRaw) {
        final Level world = source.getLevel();

        return TreeHelper.getBestGuessSpecies(world, pos).ifValidElse(species -> {
                    final Optional<JoCode> joCode = TreeHelper.getJoCode(world, pos);

                    if (codeRaw) {
                        sendSuccess(source, new TextComponent(joCode.map(JoCode::toString).orElse("?")));
                    } else {
                        sendSuccess(source, new TranslatableComponent("commands.dynamictrees.success.get_tree",
                                species.getTextComponent(), joCode.map(JoCode::getTextComponent)
                                .orElse(new TextComponent("?"))));
                    }
                }, () -> sendFailure(source, new TranslatableComponent("commands.dynamictrees.error.get_tree",
                        CommandHelper.posComponent(pos).copy().withStyle(style -> style.withColor(ChatFormatting.DARK_RED))))
        ) ? 1 : 0;
    }

}
