package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ITreePart {

	/**
	* Get a cell that provides the level of hydration to neighboring structures
	*
	* @param blockAccess Readonly access to blocks
	* @param pos Position of the cell
	* @param blockState the blockState of the block we are getting the cell from
	* @param dir The direction of the request(opposite the direction of the requester)
	* @param leavesTree The tree data of the leaves the request came from
	* @return Cell for getting hydration level
	*/
	ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree);

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
	int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from);

	/**
	* The radius of the part that a neighbor is expected to connect with 
	* @param world The current world
	* @param pos Position
	* @param branch The branch making the request
	* @param fromRadius The radius of the branch requesting connection data
	* @return Radius of the connection point to this block from the branch
	*/
	int getRadiusForConnection(IBlockAccess world, BlockPos pos, BlockBranch from, int fromRadius);

	/**
	* Used to get the radius of branches.. all other treeparts will/should return 0
	* @param blockAccess Readonly access to blocks
	* @param pos Position
	* @return Radius of the treepart(branch)
	*/
	int getRadius(IBlockAccess blockAccess, BlockPos pos);

	/**
	* Configurable general purpose branch network scanner to gather data and/or perform operations
	* 
	* @param world The current world
	* @param pos Position
	* @param fromDir The direction that should not be analyzed.  Pass null to analyse in all directions
	* @param signal The Mapping Signal object to gather data and/or perform operations
	* @return
	*/
	MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal);

	/**
	* Get the appropriate dynamic tree this block is used to build.
	*  
	* @param blockAccess Readonly access to blocks
	* @param pos Position
	* @return DynamicTree
	*/
	DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos);
	
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
	int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius);

	/**
	* The single root node of a tree.
	* 
	* @return true if treepart is root node. false otherwise.
	*/
	boolean isRootNode();
	
	
	boolean isBranch();

}
