package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.CommonBlockStates;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * @author Harley O'Connor
 */
public class DenuderNode implements INodeInspector {

    private final Species species;
    private final Family family;

    public DenuderNode(final Species species, final Family family) {
        this.species = species;
        this.family = family;
    }

    @Override
    public boolean run(BlockState state, IWorld world, BlockPos pos, Direction fromDir) {
        final BranchBlock branch = TreeHelper.getBranch(state);

        if (branch == null || branch != this.family.getBranch())
            return false;

        final int radius = branch.getRadius(state);

        branch.stripBranch(state, world, pos, radius);

        if (radius <= this.family.getPrimaryThickness()) {
            this.removeSurroundingLeaves(world, pos);
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
        return false;
    }

    private static final int TEST_LEAVES_RADIUS = 3;

    public void removeSurroundingLeaves(IWorld world, BlockPos twigPos) {
        if (world.isClientSide())
            return;

        BlockPos.betweenClosedStream(twigPos.offset(-TEST_LEAVES_RADIUS, -TEST_LEAVES_RADIUS, -TEST_LEAVES_RADIUS), twigPos.offset(TEST_LEAVES_RADIUS, TEST_LEAVES_RADIUS, TEST_LEAVES_RADIUS)).forEach(leavesPos -> {
            // We're only interested in where leaves could possibly be.
            if (this.species.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, leavesPos) == 0)
                return;

            final BlockState state = world.getBlockState(leavesPos);
            if (this.family.isCompatibleGenericLeaves(this.species, state, world, leavesPos)) {
                world.setBlock(leavesPos, CommonBlockStates.AIR, 3);
            }
        });
    }

}
