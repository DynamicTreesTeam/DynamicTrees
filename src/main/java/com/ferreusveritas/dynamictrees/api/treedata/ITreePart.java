package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import com.ferreusveritas.dynamictrees.systems.*;

import javax.annotation.Nullable;

public interface ITreePart {

	/**
	 * Get a cell that provides the level of hydration to neighboring structures
	 *
	 * @param blockAccess Readonly access to blocks
	 * @param pos Position of the cell
	 * @param blockState the blockState of the block we are getting the cell from
	 * @param dir The direction of the request(opposite the direction of the requester)
	 * @param leavesProperties The tree data of the leaves the request came from
	 * @return Cell for getting hydration level
	 */
	ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, LeavesProperties leavesProperties);

	/**
	 * The signal that is passed from the root of the tree to the tip of a branch to create growth.
	 *
	 * @param world The current world
	 * @param pos Position
	 * @param signal Signal structure that keeps track of the growth path
	 * @return Signal parameter for chaining
	 */
	GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal);

	/**
	 * The probability that the branch logic will follow into this block as part of it's path.
	 *
	 * @param blockAccess Readonly access to blocks
	 * @param pos Position
	 * @param from The branch making the request
	 * @return Probability weight used to determine if the growth path will take this block as a path next.
	 */
	int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from);

	/**
	 * The radius of the part that a neighbor is expected to connect with
	 * @param world The current world
	 * @param pos Position
	 * @param from The branch making the request
	 * @param side The side the block is requesting(relative to the requesting block)
	 * @param fromRadius The radius of the branch requesting connection data
	 * @return Radius of the connection point to this block from the branch
	 */
	int getRadiusForConnection(BlockState blockState, IBlockReader world, BlockPos pos, BranchBlock from, Direction side, int fromRadius);

	/**
	 * Used to get the radius of branches.. all other treeparts will/should return 0
	 * @param blockState the blockState of the block we are trying to get the radius of.
	 * @return Radius of the treepart(branch)
	 */
	int getRadius(BlockState blockState);

	/**
	  * Whether this node should be analyzed or not.
	  * Branches should always be true.  Leaves should always be false.
	  * Other types may vary in return depending on implementation.
	  *
	  * @return
	  */
	boolean shouldAnalyse(BlockState blockState, IBlockReader blockAccess, BlockPos pos);

	/**
	 * Configurable general purpose branch network scanner to gather data and/or perform operations
	 *
	 * @param world The current world
	 * @param pos Position
	 * @param fromDir The direction that should not be analyzed.  Pass null to analyse in all directions
	 * @param signal The Mapping Signal object to gather data and/or perform operations
	 * @return
	 */
	MapSignal analyse(BlockState blockState, IWorld world, BlockPos pos, @Nullable Direction fromDir, MapSignal signal);

	/**
	 * Get the appropriate {@link Family} this block is used to build.
	 *
	 * @param blockAccess Readonly access to blocks
	 * @param pos Position
	 * @return DynamicTree
	 */
	Family getFamily(BlockState blockState, IBlockReader blockAccess, BlockPos pos);

	/**
	 * A branch requires 2 or more adjacent supporting neighbors at least one of which must be another branch
	 * Valid supports are other branches(always), leaves(for twigs), and rooty dirt(under special circumstances)
	 * return value is nybbled neighbor values. High Nybble(0xF0) is count of branches only, Low Nybble(0x0F) is any valid reinforcing treepart(including branches)
	 *
	 * @param blockAccess Readonly access to blocks
	 * @param branch The branch making the request
	 * @param pos Position
	 * @param dir The direction of the request(opposite the direction of the requester)
	 * @param radius The radius of the branch requesting support
	 * @return Neighbor values in Nybble pair ( (#branches & 0xF0) | (#treeparts & 0x0F) )
	 */
	int branchSupport(BlockState blockState, IBlockReader blockAccess, BranchBlock branch, BlockPos pos, Direction dir, int radius);

	enum TreePartType {
		NULL, // Not an official tree part
		ROOT, // Anything based off of BlockRooty
		BRANCH, // Anything based off of BlockBranch
		LEAVES, // Anything based off of BlockDynamicLeaves
		OTHER // Anything else
	}

	TreePartType getTreePartType();

	default boolean isRootNode() {
		return false;
	}

}
