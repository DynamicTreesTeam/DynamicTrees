package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.tree.species.Species;
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
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null && species.getFamily() == branch.getFamily()) {
            if (branch.getRadius(state) == 1) {
                level.removeBlock(pos, false);//Destroy the thin branch
            }
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

}
