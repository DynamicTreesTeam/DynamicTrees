package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.Cell;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Family;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class NullTreePart implements TreePart {

    //This is a safe dump for blocks that aren't tree parts
    //Handles some vanilla blocks

    @Override
    public Cell getHydrationCell(BlockGetter reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(Level world, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        // Connectable blocks such as bee nests and shroomlight will be handled here.
        if (BranchConnectables.isBlockConnectable(state.getBlock())) {
            int rad = BranchConnectables.getConnectionRadiusForBlock(state, reader, pos, side);
            if (rad > 0) {
                return rad;
            }
        }

        return 0;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter reader, BlockPos pos, BranchBlock from) {
        return state.isAir() ? 1 : 0;
    }

    @Override
    public int getRadius(BlockState state) {
        return 0;
    }

    @Override
    public boolean shouldAnalyse(BlockState state, BlockGetter reader, BlockPos pos) {
        return BranchConnectables.isBlockConnectable(state.getBlock());
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor world, BlockPos pos, Direction fromDir, MapSignal signal) {
        signal.run(state, world, pos, fromDir);
        return signal;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public Family getFamily(BlockState state, BlockGetter reader, BlockPos pos) {
        return Family.NULL_FAMILY;
    }

    public final TreePartType getTreePartType() {
        return TreePartType.NULL;
    }
}
