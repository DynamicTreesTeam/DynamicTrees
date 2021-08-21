package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * @author Harley O'Connor
 */
public class DenuderNode implements NodeInspector {

    private final Species species;
    private final Family family;

    public DenuderNode(final Species species, final Family family) {
        this.species = species;
        this.family = family;
    }

    @Override
    public boolean run(BlockState state, IWorld world, BlockPos pos, Direction fromDir) {
        final BranchBlock branch = TreeHelper.getBranch(state);

        if (branch == null || branch != this.family.getBranch()) {
            return false;
        }

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

    public void removeSurroundingLeaves(IWorld world, BlockPos twigPos) {
        if (world.isClientSide()) {
            return;
        }

        final SimpleVoxmap leafCluster = this.species.getLeavesProperties().getCellKit().getLeafCluster();
        final int xBound = leafCluster.getLenX();
        final int yBound = leafCluster.getLenY();
        final int zBound = leafCluster.getLenZ();

        BlockPos.betweenClosedStream(twigPos.offset(-xBound, -yBound, -zBound), twigPos.offset(xBound, yBound, zBound)).forEach(testPos -> {
            // We're only interested in where leaves could possibly be.
            if (leafCluster.getVoxel(twigPos, testPos) == 0) {
                return;
            }

            final BlockState state = world.getBlockState(testPos);
            if (this.family.isCompatibleGenericLeaves(this.species, state, world, testPos)) {
                world.setBlock(testPos, BlockStates.AIR, 3);
            }
        });
    }

}
