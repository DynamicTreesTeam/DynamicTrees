package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Family;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NullTreePart implements ITreePart {
	
	//This is a safe dump for blocks that aren't tree parts
	//Handles some vanilla blocks
	
	@Override
	public ICell getHydrationCell(IBlockReader reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
		return CellNull.NULL_CELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		//Connectable blocks such as bee nests and shroomlight will be handled here.
		if (BranchConnectables.isBlockConnectable(state.getBlock())){
			int rad = BranchConnectables.getConnectionRadiusForBlock(state, reader, pos, side);
			if (rad > 0) return rad;
		}
		return 0;
	}
	
	@Override
	public int probabilityForBlock(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from) {
		return state.getBlock().isAir(state, reader, pos) ? 1 : 0;
	}
	
	@Override
	public int getRadius(BlockState state) {
		return 0;
	}
	
	@Override
	public boolean shouldAnalyse(BlockState state, IBlockReader reader, BlockPos pos) {
		return BranchConnectables.isBlockConnectable(state.getBlock());
	}
	
	@Override
	public MapSignal analyse(BlockState state, IWorld world, BlockPos pos, Direction fromDir, MapSignal signal) {
		signal.run(state, world, pos, fromDir);
		return signal;
	}
	
	@Override
	public int branchSupport(BlockState state, IBlockReader reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
		return 0;
	}
	
	@Override
	public Family getFamily(BlockState state, IBlockReader reader, BlockPos pos) {
		return Family.NULL_FAMILY;
	}
	
	public final TreePartType getTreePartType() {
		return TreePartType.NULL;
	}
	
}
