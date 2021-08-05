package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ITreePart {

	/**
	 * Returns an {@link ICell} that provides the level of hydration to neighboring structures.
	 *
	 * @param world            read-only access to blocks
	 * @param pos              the position of the block at which to gather surrounding cells
	 * @param state            the state of the block we are getting the cell from
	 * @param dir              The direction of the request (opposite the direction of the requester)
	 * @param leavesProperties the properties of the leaves the request came from
	 * @return the cell for getting hydration levels
	 */
	ICell getHydrationCell(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing dir, ILeavesProperties leavesProperties);

	/**
	 * The signal that is passed from the root of the tree to the tip of a branch to create growth.
	 *
	 * @param world  the current world
	 * @param pos    the position of this {@link ITreePart}
	 * @param signal the signal that keeps track of the growth path
	 * @return the specified {@code signal} for chaining
	 */
	GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal);

	/**
	 * Returns the probability that the branch logic will follow into this block as part of its path.
	 *
	 * @param state the state of this {@link ITreePart}
	 * @param world read-only access to blocks
	 * @param pos   the position of this {@link ITreePart}
	 * @param from  the branch making the request
	 * @return the probability weight used to determine if the growth path will take this block as a path next
	 */
	int probabilityForBlock(IBlockState state, IBlockAccess world, BlockPos pos, BlockBranch from);

	/**
	 * Returns the radius of the {@link ITreePart} that a neighbor is expected to connect with.
	 *
	 * @param state      the state of this {@link ITreePart}
	 * @param world      the current world
	 * @param pos        the position of this {@link ITreePart}
	 * @param from       the branch making the request
	 * @param side       the side the block is requesting (relative to the requesting block)
	 * @param fromRadius the radius of the branch requesting connection data
	 * @return the radius of the connection point to this block from the branch
	 */
	int getRadiusForConnection(IBlockState state, IBlockAccess world, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius);

	/**
	 * Returns the radius of a branch, or {@code 0} if this {@link ITreePart} is not a {@link BlockBranch} instance.
	 *
	 * @param state the state of the block we are trying to get the radius of
	 * @return the radius of the branch, or {@code 0} if this is not a branch
	 */
	int getRadius(IBlockState state);

	/**
	 * Returns whether this node should be analyzed. Branches should always return {@code true} where leaves should
	 * always return {@code false}. Other types may vary in return depending on the implementation.
	 *
	 * @return {@code true} if this part should be analysed; {@code false} if not
	 */
	boolean shouldAnalyse();

	/**
	 * Configurable, general-purpose branch network scanner to gather data and/or perform operations on a dynamic tree.
	 *
	 * @param state   the state of this {@link ITreePart}
	 * @param world   the current world
	 * @param pos     the position of this {@link ITreePart}
	 * @param fromDir the direction that should not be analyzed; {@code null} to analyse in all directions
	 * @param signal  the map signal object to gather data and/or perform operations
	 * @return the specified {@code signal} for chaining
	 */
	MapSignal analyse(IBlockState state, World world, BlockPos pos, EnumFacing fromDir, MapSignal signal);

	/**
	 * Returns the {@link TreeFamily} this block is used to build.
	 *
	 * @param state the state of this {@link ITreePart}
	 * @param world read-only access to blocks
	 * @param pos   the position of this {@link ITreePart}
	 * @return DynamicTree
	 */
	TreeFamily getFamily(IBlockState state, IBlockAccess world, BlockPos pos);

	/**
	 * A branch requires 2 or more adjacent supporting neighbors, at least one of which must be another branch. Valid
	 * supports are other branches (always), leaves (for twigs), and rooty dirt (under special circumstances).
	 * <p>
	 * The return value is nybbled neighbor values. High Nybble (0xF0) is count of branches only, Low Nybble (0x0F) is
	 * any valid reinforcing {@link ITreePart} (including branches).
	 *
	 * @param state  the state of this {@link ITreePart}
	 * @param world  read-only access to blocks
	 * @param branch the branch making the request
	 * @param pos    the position of this {@link ITreePart}
	 * @param dir    the direction of the request (opposite the direction of the requester)
	 * @param radius the radius of the branch requesting support
	 * @return the neighbor values in Nybble pair ( (#branches & 0xF0) | (#treeparts & 0x0F) )
	 */
	int branchSupport(IBlockState state, IBlockAccess world, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius);

	enum TreePartType {
		/**
		 * Not an official part of a tree.
		 */
		NULL,
		/**
		 * A rooty block, extending {@link BlockRooty}.
		 */
		ROOT,
		/**
		 * A branch block, extending {@link BlockBranch}.
		 */
		BRANCH,
		/**
		 * A leaves block, extending {@link BlockDynamicLeaves}.
		 */
		LEAVES,
		/**
		 * Another part of the tree.
		 */
		OTHER
	}

	TreePartType getTreePartType();

	default boolean isRootNode() {
		return false;
	}

}
