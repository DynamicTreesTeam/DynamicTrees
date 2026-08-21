package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TransformNode implements NodeInspector {

    private final Species fromSpecies;
    private final Species toSpecies;

    public TransformNode(Species fromTree, Species toTree) {
        this.fromSpecies = fromTree;
        this.toSpecies = toTree;
    }

    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null && fromSpecies.getFamily() == branch.getFamily()) {
            int radius = branch.getRadius(state);
            if (radius > 0) {
                BranchBlock newBranchBlock = toSpecies.getFamily().getBranch().orElse(null);

                // If the branch is stripped, make the replacement branch stripped.
                if (fromSpecies.getFamily().getStrippedBranch().orElse(null) == branch) {
                    newBranchBlock = toSpecies.getFamily().getStrippedBranch().get();
                }

                newBranchBlock.setRadius(level, pos, radius, null);
                if (radius == 1) {
                    transformSurroundingLeaves(level, pos);
                }
            }
        }

        return true;
    }

    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    private static final int TEST_LEAVES_RADIUS = 3;

    public void transformSurroundingLeaves(LevelAccessor world, BlockPos twigPos) {
        if (world.isClientSide()) {
            return;
        }

        final SimpleVoxmap leafCluster = this.fromSpecies.getLeavesProperties().getCellKit().getLeafCluster();
        final int xBound = leafCluster.getLenX();
        final int yBound = leafCluster.getLenY();
        final int zBound = leafCluster.getLenZ();

        BlockPos.betweenClosedStream(twigPos.offset(-xBound, -yBound, -zBound), twigPos.offset(xBound, yBound, zBound)).forEach(testPos -> {
            // We're only interested in where leaves could possibly be.
            if (this.fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, testPos) == 0) {
                return;
            }

            final BlockState state = world.getBlockState(testPos);
            if (fromSpecies.getFamily().isCompatibleGenericLeaves(this.fromSpecies, state, world, testPos)) {
                final int hydro = state.getBlock() instanceof DynamicLeavesBlock ? state.getValue(DynamicLeavesBlock.DISTANCE) : 2;
                world.setBlock(testPos, toSpecies.getLeavesProperties().getDynamicLeavesState(hydro), 3);
            }
        });
    }

}
