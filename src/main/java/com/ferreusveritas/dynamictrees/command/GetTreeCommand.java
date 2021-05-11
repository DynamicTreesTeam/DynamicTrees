package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Optional;

public final class GetTreeCommand extends SubCommand {

    @Override
    protected String getName () {
        return CommandConstants.GET_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String CODE_RAW = "code_raw";

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().executes(context -> this.getTree(context.getSource(), blockPosArgument(context), false))
                .then(booleanArgument(CODE_RAW).executes(context -> this.getTree(context.getSource(), blockPosArgument(context),
                        booleanArgument(context, CODE_RAW))));
    }

    private int getTree(final CommandSource source, final BlockPos pos, final boolean codeRaw) {
        final World world = source.getLevel();

        return TreeHelper.getBestGuessSpecies(world, pos).ifValidElse(species -> {
            final Optional<JoCode> joCode = TreeHelper.getJoCode(world, pos);

            if (codeRaw) {
                sendSuccess(source, new StringTextComponent(joCode.map(JoCode::toString).orElse("?")));
            } else {
                sendSuccess(source, new TranslationTextComponent("commands.dynamictrees.success.get_tree",
                        species.getTextComponent(), joCode.map(JoCode::getTextComponent)
                        .orElse(new StringTextComponent("?"))));
            }
        }, () -> sendFailure(source, new TranslationTextComponent("commands.dynamictrees.error.get_tree",
                CommandHelper.posComponent(pos).copy().withStyle(style -> style.withColor(TextFormatting.DARK_RED))))
        ) ? 1 : 0;
    }

}
