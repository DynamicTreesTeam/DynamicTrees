package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class GrowPulseCommand extends SubCommand {

    public GrowPulseCommand () {
        this.takesCoordinates = true;
        this.defaultToExecute = false;
    }

    @Override
    protected String getName() {
        return CommandConstants.GROW_PULSE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        final World world = context.getSource().getWorld();
        final BlockPos pos = this.getPositionArg(context);
        final BlockState state = world.getBlockState(pos);

        if (TreeHelper.getTreePart(state) == TreeHelper.NULL_TREE_PART && !(state.getBlock() instanceof TrunkShellBlock)) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return 0;
        }

        BlockPos rootPos = TreeHelper.findRootNode(world, pos);

        if (!rootPos.equals(BlockPos.ZERO))
            TreeHelper.growPulse(world, rootPos);

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
