package com.ferreusveritas.dynamictrees.api.treedata;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

public interface ILeavesProperties {

	/** The type of tree these leaves connect to */
	ILeavesProperties setTree(TreeFamily tree);

//	/** This is needed so the {@link BlockDynamicLeaves} knows if it can pull hydro from a branch */
	TreeFamily getTree();

	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	BlockState getPrimitiveLeaves();

	/** cached ItemStack of primitive leaves(what is returned when leaves are sheared) */
	ItemStack getPrimitiveLeavesItemStack();

	ILeavesProperties setDynamicLeavesState(BlockState state);

	BlockState getDynamicLeavesState();

	BlockState getDynamicLeavesState(int distance);

	int getFlammability();

	int getFireSpreadSpeed();

	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies. Set to zero to disable smothering. [default = 4] **/
	int getSmotherLeavesMax();

	/** Minimum amount of light necessary for a leaves block to be created. [default = 13] **/
	int getLightRequirement();

	/** A CellKit for leaves automata */
	ICellKit getCellKit();

//	@OnlyIn(Dist.CLIENT)
	int foliageColorMultiplier(BlockState state, IEnviromentBlockReader reader, BlockPos pos);

	/**
	 * Allows the leaves to perform a specific needed behavior or to optionally cancel the update
	 *
	 * @param worldIn
	 * @param pos
	 * @param state
	 * @param rand
	 * @return return true to allow the normal DynamicLeavesBlock update to occur
	 */
	boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand);

	int getRadiusForConnection(BlockState blockState, IBlockReader blockReader, BlockPos pos, BlockBranch from, Direction side, int fromRadius);

}