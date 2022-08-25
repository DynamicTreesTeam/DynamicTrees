package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.killTree(context.getSource(), rootPosArgument(context))));
    }

    private void killTree(final CommandSourceStack source, final BlockPos rootPos) {
        final Level world = source.getLevel();

        Objects.requireNonNull(TreeHelper.getRooty(world.getBlockState(rootPos))).destroyTree(world, rootPos);
        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.kill_tree",
                CommandHelper.posComponent(rootPos, ChatFormatting.AQUA)));
    }

}
