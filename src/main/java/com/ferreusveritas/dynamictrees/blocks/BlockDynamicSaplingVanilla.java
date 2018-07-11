package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockDynamicSaplingVanilla extends BlockDynamicSapling {
	
	protected Map<Integer, Species> trees = new HashMap<Integer, Species>();
	
	public BlockDynamicSaplingVanilla(String name) {
		super(name);
		setDefaultState(this.blockState.getBaseState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK));
	}
	
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	@Override
	public Species getSpecies(IBlockAccess access, BlockPos pos, IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getValue(BlockSapling.TYPE).ordinal());
		}
		return trees.get(0);
	}
	
	@Override
	public BlockDynamicSaplingVanilla setSpecies(IBlockState state, Species species) {
		if(state.getBlock() == this) {
			trees.put(state.getValue(BlockSapling.TYPE).ordinal(), species);
		}
		return this;
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(meta & 0xF));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockSapling.TYPE).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {BlockSapling.TYPE});
	}
    
}
