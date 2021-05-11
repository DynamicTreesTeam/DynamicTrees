package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Objects;

public final class KillTreeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.KILL_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.killTree(context.getSource(), rootPosArgument(context))));
    }

    private void killTree(final CommandSource source, final BlockPos rootPos) {
        final World world = source.getLevel();

        Objects.requireNonNull(TreeHelper.getRooty(world.getBlockState(rootPos))).destroyTree(world, rootPos);
        sendSuccessAndLog(source, new TranslationTextComponent("commands.dynamictrees.success.kill_tree",
                CommandHelper.posComponent(rootPos, TextFormatting.AQUA)));
    }

}
