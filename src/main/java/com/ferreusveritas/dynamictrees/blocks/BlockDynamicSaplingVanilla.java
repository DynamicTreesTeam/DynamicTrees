package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockDynamicSaplingVanilla extends BlockDynamicSapling {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
	public BlockDynamicSaplingVanilla(String name) {
		super(name);
		setDefaultState(this.blockState.getBaseState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK));
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public DynamicTree getTree(IBlockState state) {
		if(state.getBlock() == this) {
			return trees.get(state.getValue(BlockSapling.TYPE).ordinal());
		}
    	return trees.get(0);
	}

	@Override
	public BlockDynamicSaplingVanilla setTree(IBlockState state, DynamicTree tree) {
		if(state.getBlock() == this) {
			trees.put(state.getValue(BlockSapling.TYPE).ordinal(), tree);
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
