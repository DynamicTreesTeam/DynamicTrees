package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Destroys all thin(radius == 1) branches on a tree.. leaving it to postRot.
 *
 * @author ferreusveritas
 */
public class DiseaseNode implements NodeInspector {

    Species species;//Destroy any thin branches made of the same kind of wood.

    public DiseaseNode(Species tree) {
        this.species = tree;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null && species.getFamily() == branch.getFamily()) {
            if (branch.getRadius(blockState) == 1) {
                world.removeBlock(pos, false);//Destroy the thin branch
            }
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
