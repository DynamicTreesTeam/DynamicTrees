package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILeavesProperties {

	ILeavesProperties setTree(DynamicTree tree);
	
	/** This is needed so the {@link BlockDynamicLeaves} knows if it can pull hydro from a branch */
	DynamicTree getTree();
	
	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	IBlockState getPrimitiveLeaves();

	/** cached ItemStack of primitive leaves(what is returned when leaves are sheared) */
	ItemStack getPrimitiveLeavesItemStack();
			
	ILeavesProperties setDynamicLeavesState(IBlockState state);
	
	IBlockState getDynamicLeavesState();

	IBlockState getDynamicLeavesState(int hydro);
	
	int getFlammability();
	
	int getFireSpreadSpeed();
	
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	int getSmotherLeavesMax();
	
	/** Minimum amount of light necessary for a leaves block to be created. [default = 13] **/
	int getLightRequirement();
	
	/** A CellKit for leaves automata */
	ICellKit getCellKit();
	
	@SideOnly(Side.CLIENT)
	int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos);
	
}
