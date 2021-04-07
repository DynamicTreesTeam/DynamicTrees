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

public final class GetTreeCommand extends SubCommand {

    @Override
    protected String getName () {
        return CommandConstants.GET_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().executes(context -> this.getTree(context.getSource(), blockPosArgument(context)));
    }

    private int getTree(final CommandSource source, final BlockPos pos) {
        final World world = source.getLevel();

        return TreeHelper.getBestGuessSpecies(world, pos).ifValidElse(species ->
                        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.get_tree",
                                species.getTextComponent(), TreeHelper.getJoCode(world, pos).map(JoCode::getTextComponent)
                                .orElse(new StringTextComponent("?"))), false),
                () -> source.sendFailure(new TranslationTextComponent("commands.dynamictrees.error.get_tree",
                        CommandHelper.posComponent(pos).copy().withStyle(style -> style.withColor(TextFormatting.DARK_RED))))
        ) ? 1 : 0;
    }

}
