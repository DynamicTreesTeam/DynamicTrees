package com.ferreusveritas.dynamictrees.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree.DestroyType;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeCollector;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData.PosType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkTreeHelper {
	
	private static final int chunkWidth = 16;
	
	/**
	 * Removes floating little bits of tree that have somehow lost
	 * connection with their parent root system.
	 * 
	 * @param world The world
	 * @param cPos The chunk position where the effect is intended
	 * @param radius Radius of effect in chunk width units
	 */
	public static void removeOrphanedBranchNodes(World world, ChunkPos cPos, int radius) {
		
		if (cPos == null) {//Who would be so unkind?
			throw new NullPointerException("Null Chunk Position");
		}
		
		Set<BlockPos> found = new HashSet<>();//This is used to track branches that are already proven
		
		BlockBounds bounds = getEffectiveBlockBounds(world, cPos, radius);
		
		for (MutableBlockPos pos : bounds.iterate()) {
			if (found.contains(pos)) {
				continue;//Block was already proven to be part of a valid tree structure
			}
			
			// Test if there's a branch block at this position
			IBlockState state = world.getBlockState(pos);
			Optional<BlockBranch> branchBlock = TreeHelper.getBranchOpt(state);
			if (!branchBlock.isPresent()) {
				continue;// No branch block found at this position.  Move on
			}
			
			// Test if the branch has a root node attached to it
			BlockPos rootPos = TreeHelper.findRootNode(world, pos);
			if (rootPos == BlockPos.ORIGIN) {// If the root position is the ORIGIN object it means that no root block was found
				// If the root node isn't found then all nodes are orphan.  Destroy the entire network.
				doTreeDestroy(world, branchBlock, rootPos);
				continue;
			}
			
			// There is at least one root block in the network
			IBlockState rootyState = world.getBlockState(rootPos);
			Optional<BlockRooty> rootyBlock = TreeHelper.getRootyOpt(rootyState);
			if (!rootyBlock.isPresent()) {
				continue;//This theoretically shouldn't ever happen
			}
			
			// Rooty block confirmed, build details about the trunk coming out of it
			EnumFacing trunkDir = rootyBlock.get().getTrunkDirection(world, rootPos);
			BlockPos trunkPos = rootPos.offset(trunkDir);
			IBlockState trunkState = world.getBlockState(trunkPos);
			Optional<BlockBranch> trunk = TreeHelper.getBranchOpt(trunkState);
			
			if (!trunk.isPresent()) {
				continue;//This theoretically shouldn't ever happen
			}
			
			// There's a trunk coming out of the rooty block, that's kinda expected.  But is it the only rooty block in the network?
			MapSignal signal = new MapSignal();
			signal.destroyLoopedNodes = false;
			trunk.get().analyse(trunkState, world, trunkPos, null, signal);
			if (signal.multiroot || signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network
				doTreeDestroy(world, branchBlock, pos);
				continue;
			} else { //Tree appears healthy with only a single attached root block
				trunk.get().analyse(trunkState, world, trunkPos, null, new MapSignal(new NodeCollector(found)));
			}
		}
	}

	/**
	 * Removes all trees that have branches in a chunk area.
	 * to prevent partial trees in adjacent chunks.  Useful
	 * for removing trees prior to pruning world chunks.
	 * 
	 * @param world The world
	 * @param cPos The chunk position where the effect is intended
	 * @param radius Radius of effect in chunk width units
	 */
	public static void removeAllBranchesFromChunk(World world, ChunkPos cPos, int radius) {
		
		if (cPos == null) {//Who would be so unkind?
			throw new NullPointerException("Null Chunk Position");
		}
		
		BlockBounds bounds = getEffectiveBlockBounds(world, cPos, radius);
		
		for (MutableBlockPos pos : bounds.iterate()) {
			IBlockState state = world.getBlockState(pos);
			Optional<BlockBranch> branchBlock = TreeHelper.getBranchOpt(state);
			if (branchBlock.isPresent()) {
				doTreeDestroy(world, branchBlock, pos);
			}
		}
	}
	
	public static BlockBounds getEffectiveBlockBounds(World world, ChunkPos cPos, int radius) {
		Chunk chunk = world.getChunkFromChunkCoords(cPos.x, cPos.z);
		BlockBounds bounds = new BlockBounds(world, cPos);
		
		bounds.shrink(EnumFacing.UP, (world.getHeight() - 1) - (chunk.getTopFilledSegment() + 16));
		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			bounds.expand(dir, radius * chunkWidth);
		}
		
		return bounds;
	}
	
	private static void doTreeDestroy(World world, Optional<BlockBranch> branchBlock, BlockPos pos) {
		BranchDestructionData destroyData = branchBlock.get().destroyBranchFromNode(world, pos, EnumFacing.DOWN, true);
		destroyData.leavesDrops.clear();// Prevent dropped seeds from planting themselves again
		EntityFallingTree.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);//Destroy the tree client side without fancy effects
		cleanupNeighbors(world, destroyData);
	}
	
	private static final byte NONE = (byte)0;
	private static final byte TREE = (byte)1;
	private static final byte SURR = (byte)2;
	
	public static void cleanupNeighbors(World world, BranchDestructionData destroyData) {
		
		// Only run on the server since the block updates will come from the server anyway
		if(world.isRemote) {
			return;
		}
		
		// Get the bounds of the tree, all leaves and branches but not the rooty block
		BlockBounds treeBounds = new BlockBounds(destroyData.cutPos);
		destroyData.getPositions(PosType.LEAVES, true).forEach(pos -> treeBounds.union(pos));
		destroyData.getPositions(PosType.BRANCHES, true).forEach(pos -> treeBounds.union(pos));
		treeBounds.expand(1); // Expand by one to contain the 3d "outline" of the voxels
		
		// Mark voxels for leaves or branch blocks
		SimpleVoxmap treeVoxmap = new SimpleVoxmap(treeBounds);
		destroyData.getPositions(PosType.LEAVES, true).forEach(pos -> treeVoxmap.setVoxel(pos, TREE));
		destroyData.getPositions(PosType.BRANCHES, true).forEach(pos -> treeVoxmap.setVoxel(pos, TREE));
		
		// Set voxels in the outline map for any adjacent voxels from the source tree map
		SimpleVoxmap outlineVoxmap = new SimpleVoxmap(treeVoxmap);
		treeVoxmap.getAllNonZero(TREE).forEach(pos -> {
			for(EnumFacing dir : EnumFacing.VALUES) {
				outlineVoxmap.setVoxel(pos.add(dir.getDirectionVec()), SURR);
			}
		});
		
		// Clear out the original positions of the leaves and branch blocks since they've already been deleted
		treeVoxmap.getAllNonZero(TREE).forEach(pos -> outlineVoxmap.setVoxel(pos, NONE));
		
		// Finally use this map for cleaning up marked block positions
		outlineVoxmap.getAllNonZero(SURR).forEach(pos -> cleanupBlock(world, pos));
	}
	
	/**
	 * Cleanup blocks that are attached(or setting on) various parts of the tree
	 * 
	 * @param world
	 * @param pos
	 */
	public static void cleanupBlock(World world, BlockPos pos) {
		IBlockState blockstate = world.getBlockState(pos);
		if(blockstate == ModBlocks.blockStates.air) { // This is the most likely case so bail early
			return;
		}
		
		Block block = blockstate.getBlock();
		
		// Cleanup snow layers, hanging fruit(apples), trunk fruit(cocoa), and surface roots.
		if(block instanceof BlockSnow || block instanceof BlockFruit || block instanceof BlockFruitCocoa || block instanceof BlockSurfaceRoot) {
			world.setBlockState(pos, ModBlocks.blockStates.air, 2);
		}
		// Cleanup vines
		else if(block instanceof BlockVine) {
			cleanupVines(world, pos);
		}
	}
	
	/**
	 * Cleanup vines starting the the top and moving down until a vine block is no longer found
	 * 
	 * @param world
	 * @param pos
	 */
	public static void cleanupVines(World world, BlockPos pos) {
		MutableBlockPos mblock = new MutableBlockPos(pos);// Mutable because ZOOM!
		while(world.getBlockState(mblock).getBlock() instanceof BlockVine) {// BlockVine instance helps with modded vine types
			world.setBlockState(mblock, ModBlocks.blockStates.air, 2);
			mblock.move(EnumFacing.DOWN);
		}
	}
	
}
