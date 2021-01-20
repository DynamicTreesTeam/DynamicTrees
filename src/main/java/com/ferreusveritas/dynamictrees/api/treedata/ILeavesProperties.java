package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public interface ILeavesProperties {

	/** The type of tree these leaves connect to */
	ILeavesProperties setTree(TreeFamily tree);

	/** This is needed so the {@link DynamicLeavesBlock} knows if it can pull hydro from a branch */
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

	/** Determines if the block is leaves or part of a giant mushroom */
	FoliageTypes getFoliageType();

	@OnlyIn(Dist.CLIENT)
	int foliageColorMultiplier(BlockState state, IBlockDisplayReader reader, BlockPos pos);

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

	int getRadiusForConnection(BlockState blockState, IBlockReader blockReader, BlockPos pos, BranchBlock from, Direction side, int fromRadius);

	enum FoliageTypes {
		LEAVES,
		FUNGUS,
		WART
	}
}