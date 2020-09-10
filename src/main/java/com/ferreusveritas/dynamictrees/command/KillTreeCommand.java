package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class KillTreeCommand extends SubCommand {

    public KillTreeCommand() {
        this.takesCoordinates = true;
    }

    @Override
    protected String getName() {
        return CommandConstants.KILL_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.nocoords", "tree"));
        return 0;
    }

    @Override
    protected int executeWithCoords(CommandContext<CommandSource> context, World worldIn, BlockPos blockPos) {
        final BlockPos rootPos = TreeHelper.findRootNode(worldIn.getBlockState(blockPos), worldIn, blockPos);

        if (rootPos == BlockPos.ZERO) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        TreeHelper.getRooty(worldIn.getBlockState(rootPos)).destroyTree(worldIn, rootPos);

        return 1;
    }

}
