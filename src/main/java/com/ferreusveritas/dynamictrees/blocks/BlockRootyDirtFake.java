package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty.IMimic;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootyDirtFake extends Block implements ITreePart, IMimic {
	
	public BlockRootyDirtFake(String name) {
		super(Material.GROUND);
		setSoundType(SoundType.GROUND);
		setTickRandomly(true);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{}, new IUnlistedProperty[] {MimicProperty.MIMIC});
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).withProperty(MimicProperty.MIMIC, getMimic(access, pos)) : state;
	}
	
	@Override
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
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
	public MapSignal analyse(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		return signal;
	}
	
	@Override
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesProperties) {
		return CellNull.NULLCELL;
	}
	
	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}
	
	@Override
	public int probabilityForBlock(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}
	
	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess world, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return 8;
	}
	
	@Override
	public int getRadius(IBlockState blockState) {
		return 8;
	}
	
	@Override
	public TreeFamily getFamily(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return TreeFamily.NULLFAMILY;
	}
	
	@Override
	public int branchSupport(IBlockState blockState, IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return BlockBranch.setSupport(1, 0);
	}
	
	@Override
	public TreePartType getTreePartType() {
		return TreePartType.OTHER;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
}
