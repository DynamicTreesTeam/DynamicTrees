package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class GrowPulseCommand extends SubCommand {

    public GrowPulseCommand () {
        this.takesCoordinates = true;
    }

    @Override
    protected String getName() {
        return CommandConstants.GROW_PULSE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.nocoords", "tree"));
        return 1;
    }

    @Override
    protected int executeWithCoords(CommandContext<CommandSource> context, World worldIn, BlockPos blockPos) {
        ITreePart part = TreeHelper.getTreePart(worldIn.getBlockState(blockPos));

        if (part == TreeHelper.nullTreePart) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        // TODO: Find out why this isn't working.
        if (part.isRootNode()) TreeHelper.growPulse(worldIn, blockPos);

        return 1;
    }

}
