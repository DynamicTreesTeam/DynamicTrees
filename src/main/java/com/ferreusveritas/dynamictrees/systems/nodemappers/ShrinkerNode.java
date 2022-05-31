package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ShrinkerNode implements NodeInspector {

    private float radius;
    Species species;

    public ShrinkerNode(Species species) {
        this.species = species;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {

        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null) {
            radius = branch.getRadius(blockState);
            if (radius > BranchBlock.MAX_RADIUS) {
                branch.setRadius(world, pos, BranchBlock.MAX_RADIUS, fromDir);
            }
        }

        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
