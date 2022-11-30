package com.ferreusveritas.dynamictrees.systems.nodemapper;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SpeciesNode implements NodeInspector {

    private Species determination = Species.NULL_SPECIES;

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {

        TreePart treePart = TreeHelper.getTreePart(state);

        switch (treePart.getTreePartType()) {
            case BRANCH:
                if (determination == Species.NULL_SPECIES) {
                    determination = TreeHelper.getBranch(treePart).getFamily().getCommonSpecies();
                }
                break;
            case ROOT:
                determination = TreeHelper.getRooty(treePart).getSpecies(level.getBlockState(pos), level, pos);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return false;
    }

    public Species getSpecies() {
        return determination;
    }

}
