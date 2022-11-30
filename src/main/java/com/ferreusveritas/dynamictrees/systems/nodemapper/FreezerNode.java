package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FreezerNode implements NodeInspector {

    private final Species species;
    private static final int freezeRadius = 3;

    public FreezerNode(Species species) {
        this.species = species;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        final BranchBlock branch = TreeHelper.getBranch(state);
        if (branch != null) {
            final int radius = branch.getRadius(state);
            if (radius == 1) {
                this.freezeSurroundingLeaves(level, branch, pos);
            }
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    // Clumsy hack to freeze leaves
    public void freezeSurroundingLeaves(LevelAccessor world, BranchBlock branch, BlockPos twigPos) {
        if (world.isClientSide()) {
            return;
        }

        final Family tree = branch.getFamily();
        BlockPos.betweenClosedStream(twigPos.offset(-freezeRadius, -freezeRadius, -freezeRadius), twigPos.offset(freezeRadius, freezeRadius, freezeRadius)).forEach(leavesPos -> {
            if (!tree.isCompatibleGenericLeaves(this.species, world.getBlockState(leavesPos), world, leavesPos)) {
                return;
            }

            final BlockState state = world.getBlockState(leavesPos);
            final DynamicLeavesBlock leaves = TreeHelper.getLeaves(state);

            if (leaves == null) {
                return;
            }

            world.setBlock(leavesPos, leaves.getProperties(state).getPrimitiveLeaves()
                    .setValue(LeavesBlock.PERSISTENT, true), 2);
        });
    }

}
