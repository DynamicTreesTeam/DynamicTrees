package com.dtteam.dynamictrees.systems.nodemapper;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RootsDestroyerNode extends FindEndsNode {

    final AerialRootsFamily family;
    public RootsDestroyerNode(AerialRootsFamily family) {
        super();
        this.family = family;
    }

    @Override
    public boolean run(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(state);

        if (branch != null) {
            level.setBlock(pos, branch.getStateForDecay(state, level, pos), 3);//Destroy the branch and notify the client
        }

        return super.run(state, level, pos, fromDir);
    }

    @Override
    public boolean returnRun(BlockState state, LevelAccessor level, BlockPos pos, Direction fromDir) {
        return super.returnRun(state, level, pos, fromDir);
    }
}
