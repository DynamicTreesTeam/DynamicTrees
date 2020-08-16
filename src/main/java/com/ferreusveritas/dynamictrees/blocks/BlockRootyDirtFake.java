package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty.IMimic;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.*;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRootyDirtFake extends Block implements ITreePart, IMimic {

	public BlockRootyDirtFake(String name) {
		super(Block.Properties.create(Material.EARTH)
				.tickRandomly()
				.sound(SoundType.GROUND)
				.hardnessAndResistance(0.2F, 3.0F));

		//setCreativeTab;

		setRegistryName(name);
	}


//	@Override
//	public void randomTick(World world, BlockPos pos, BlockState state, Random random) {
//		for(Direction dir: Direction.values()) {
//			if(TreeHelper.isBranch(world.getBlockState(pos.add(dir.getDirectionVec())))) {
//				return;
//			}
//		}
//		world.setBlockState(pos, DTRegistries.blockStates.dirt);
//	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
//	@Override
//	protected BlockStateContainer createBlockState() {
//		return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[] {MimicProperty.MIMIC});
//	}
	
	@Override
	public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getBlock().getExtendedState(state, world, pos);
	}

	@Override
	public BlockState getMimic(IBlockReader access, BlockPos pos) {
		return MimicProperty.getDirtMimic(access, pos);
	}

	///////////////////////////////////////////
	// DIRT
	///////////////////////////////////////////

	@Override
	public boolean shouldAnalyse() {
		return false;
	}

	@Override
	public MapSignal analyse(BlockState blockState, World world, BlockPos pos, Direction fromDir, MapSignal signal) {
		return signal;
	}

	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesProperties) {
		return CellNull.NULLCELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BlockBranch from, Direction side, int fromRadius) {
		return 8;
	}

	@Override
	public int getRadius(BlockState blockState) {
		return 8;
	}

	@Override
	public TreeFamily getFamily(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		return TreeFamily.NULLFAMILY;
	}

	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BlockBranch branch, BlockPos pos, Direction dir, int radius) {
		return BlockBranch.setSupport(1, 1);
	}

	@Override
	public TreePartType getTreePartType() {
		return TreePartType.OTHER;
	}

//	@Override
//	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
}
