package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.tree.species.Species;
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
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {

        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            radius = branch.getRadius(state);
            if (radius > BranchBlock.MAX_RADIUS) {
                branch.setRadius(level, pos, BranchBlock.MAX_RADIUS, fromDir);
            }
        }

        return false;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
