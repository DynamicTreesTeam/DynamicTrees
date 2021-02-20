package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.NullTreePart;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.nodemappers.TwinkleNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class TreeHelper {
	
	public static final ITreePart NULL_TREE_PART = new NullTreePart();
	
	///////////////////////////////////////////
	//CONVENIENCE METHODS
	///////////////////////////////////////////
	
	/**
	 * Convenience method to pulse a single growth cycle and age the cuboid volume.
	 * Used by growth potions, fertilizers and the dendrocoil.
	 *
	 * @param world
	 * @param rootPos
	 */
	public static void growPulse(World world, BlockPos rootPos) {
		BlockState rootyState = world.getBlockState(rootPos);
		RootyBlock dirt = TreeHelper.getRooty(rootyState);
		if(dirt != null) {
			dirt.updateTree(rootyState, world, rootPos, world.rand, false);
			ageVolume(world, rootPos, 8, 32, 1, SafeChunkBounds.ANY);//blindly age a cuboid volume
		}
	}
	
	/**
	 * Pulses an entire leafMap volume of blocks each with an age signal.
	 * Warning: CPU intensive and should be used sparingly
	 *
	 * @param world The world
	 * @param leafMap The voxel map of hydrovalues to use as a iterator
	 * @param iterations The number of times to age the map
	 */
	public static void ageVolume(IWorld world, SimpleVoxmap leafMap, int iterations, SafeChunkBounds safeBounds){
		
		//The iterMap is the voxmap we will use as a discardable.  The leafMap must survive for snow
		SimpleVoxmap iterMap = leafMap != null ? new SimpleVoxmap(leafMap) : null;
		Iterable<BlockPos.Mutable> iterable = iterMap.getAllNonZero();
		
		for(int i = 0; i < iterations; i++) {
			for(BlockPos.Mutable iPos: iterable) {
				BlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if(block instanceof DynamicLeavesBlock) {//Special case for leaves
					int prevHydro = leafMap.getVoxel(iPos);//The leafMap should contain accurate hydro data
					int newHydro = ((IAgeable)block).age(world, iPos, blockState, world.getRandom(), safeBounds);//Get new values from neighbors
					if(newHydro == -1) {
						//Leaf block died.  Take it out of the leafMap and iterMap
						leafMap.setVoxel(iPos, (byte) 0);
						iterMap.setVoxel(iPos, (byte) 0);
					} else {
						//Leaf did not die so the block is still leaves
						if(prevHydro == newHydro) { //But it didn't change
							iterMap.setVoxel(iPos, (byte) 0); //Stop iterating over it if it's not changing
						} else {//Oh wait.. it did change
							//Update both maps with this new hydro value
							leafMap.setVoxel(iPos, (byte) newHydro);
							iterMap.setVoxel(iPos, (byte) newHydro);
							//Copy all the surrounding values from the leafMap to the iterMap since they now also have potential to change
							for(Direction dir: Direction.values()) {
								BlockPos dPos = iPos.offset(dir);
								iterMap.setVoxel(dPos, leafMap.getVoxel(dPos));
							}
						}
					}
				}
				else if(block instanceof IAgeable) {//Treat as just a regular ageable block
					((IAgeable)block).age(world, iPos, blockState, world.getRandom(), safeBounds);
				} else {//You're not supposed to be here
					leafMap.setVoxel(iPos, (byte) 0);
					iterMap.setVoxel(iPos, (byte) 0);
				}
			}
		}
		
	}
	
	/**
	 * Pulses an entire cuboid volume of blocks each with an age signal.
	 * Warning: CPU intensive and should be used sparingly
	 *
	 * @param world The world
	 * @param treePos The position of the bottom most block of a trees trunk
	 * @param halfWidth The "radius" of the cuboid volume
	 * @param height The height of the cuboid volume
	 * @param iterations The number of times to age the volume
	 */
	public static void ageVolume(IWorld world, BlockPos treePos, int halfWidth, int height, int iterations, SafeChunkBounds safeBounds){
		//Slow and dirty iteration over a cuboid volume.  Try to avoid this by using a voxmap if you can
		Iterable<BlockPos> iterable = BlockPos.getAllInBoxMutable(treePos.add(new BlockPos(-halfWidth, 0, -halfWidth)), treePos.add(new BlockPos(halfWidth, height, halfWidth)));
		for(int i = 0; i < iterations; i++) {
			for(BlockPos iPos: iterable) {
				BlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if(block instanceof IAgeable) {
					((IAgeable)block).age(world, iPos, blockState, world.getRandom(), safeBounds);//Treat as just a regular ageable block
				}
			}
		}
		
	}
	
	public static Optional<JoCode> getJoCode(World world, BlockPos pos) {
		return getJoCode(world, pos, Direction.SOUTH);
	}
	
	public static Optional<JoCode> getJoCode(World world, BlockPos pos, Direction direction) {
		if(pos == null) {
			return Optional.empty();
		}
		pos = dereferenceTrunkShell(world, pos);
		BlockPos rootPos = TreeHelper.findRootNode(world, pos);
		return rootPos != BlockPos.ZERO ? Optional.of(new JoCode(world, rootPos, direction)) : Optional.empty();
	}
	
	public static BlockPos dereferenceTrunkShell(World world, BlockPos pos) {
		
		BlockState blockState = world.getBlockState(pos);
		
		if(blockState.getBlock() == DTRegistries.trunkShellBlock) {
			TrunkShellBlock.ShellMuse muse = ((TrunkShellBlock)blockState.getBlock()).getMuse(world, blockState, pos);
			if(muse != null) {
				return muse.pos;
			}
		}
		
		return pos;
	}
	
	public static Species getCommonSpecies(World world, BlockPos pos) {
		pos = dereferenceTrunkShell(world, pos);
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BranchBlock) {
			BranchBlock branch = (BranchBlock) state.getBlock();
			return branch.getFamily().getCommonSpecies();
		}
		
		return Species.NULL_SPECIES;
	}
	
	/**
	 * This is resource intensive.  Use only for interaction code.
	 * Only the root node can determine the exact species and it has
	 * to be found by mapping the branch network.
	 *
	 * This function is deprecated and will be removed in the future.
	 * Use getExactSpecies(World, BlockPos)
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	@Deprecated
	public static Species getExactSpecies(BlockState unused, World world, BlockPos pos) {
		return getExactSpecies(world, pos);
	}
	
	/**
	 * This is resource intensive.  Use only for interaction code.
	 * Only the root node can determine the exact species and it has
	 * to be found by mapping the branch network.
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static Species getExactSpecies(World world, BlockPos pos) {
		BlockPos rootPos = findRootNode(world, pos);
		
		if(rootPos != BlockPos.ZERO) {
			BlockState rootyState = world.getBlockState(rootPos);
			return TreeHelper.getRooty(rootyState).getSpecies(rootyState, world, rootPos);
		}
		return Species.NULL_SPECIES;
	}
	
	/**
	 * This is resource intensive.  Use only for interaction code.
	 * Only the root node can determine the exact species and it has
	 * to be found by mapping the branch network.  Tries to find the
	 * exact species and if that fails tries to find the common species.
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static Species getBestGuessSpecies(World world, BlockPos pos) {
		Species species = getExactSpecies(world, pos);
		return species == Species.NULL_SPECIES ? getCommonSpecies(world, pos) : species;
	}
	
	/**
	 * Find the root node of a tree.
	 *
	 * @param world The world
	 * @param pos The position being analyzed
	 * @return The position of the root node of the tree or BlockPos.ZERO if nothing was found.
	 */
	public static BlockPos findRootNode(World world, BlockPos pos) {

		pos = dereferenceTrunkShell(world, pos);
		BlockState state = world.getBlockState(pos);
		ITreePart treePart = TreeHelper.getTreePart(world.getBlockState(pos));
		
		switch(treePart.getTreePartType()) {
			case BRANCH:
				MapSignal signal = treePart.analyse(state, world, pos, null, new MapSignal());// Analyze entire tree network to find root node
				if(signal.found) {
					return signal.root;
				}
				break;
			case ROOT:
				return pos;
			default:
				return BlockPos.ZERO;
		}
		
		return BlockPos.ZERO;
	}
	
	/**
	 * Convenience function that spawns particles all over the tree branches
	 *
	 * @param world
	 * @param rootPos
	 * @param type
	 * @param num
	 */
	public static void treeParticles(World world, BlockPos rootPos, BasicParticleType type, int num) {
		if(world.isRemote) {
			startAnalysisFromRoot(world, rootPos, new MapSignal(new TwinkleNode(type, num)));
		}
	}
	
	/**
	 * Convenience function that verifies an analysis is starting from the root
	 * node before commencing.
	 *
	 * @param world The world
	 * @param rootPos The position of the rootyBlock
	 * @param signal The signal carrying the inspectors
	 * @return true if a root block was found.
	 */
	public static boolean startAnalysisFromRoot(IWorld world, BlockPos rootPos, MapSignal signal) {
		RootyBlock dirt = TreeHelper.getRooty(world.getBlockState(rootPos));
		if(dirt != null) {
			dirt.startAnalysis(world, rootPos, signal);
			return true;
		}
		return false;
	}
	
	//Treeparts
	
	public static boolean isTreePart(Block block) {
		return block instanceof ITreePart;
	}
	
	public static boolean isTreePart(BlockState blockState) {
		return isTreePart(blockState.getBlock());
	}
	
	public static boolean isTreePart(IWorld blockAccess, BlockPos pos) {
		return isTreePart(blockAccess.getBlockState(pos).getBlock());
	}
	
	public static ITreePart getTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : NULL_TREE_PART;
	}
	
	public static ITreePart getTreePart(BlockState blockState) {
		return getTreePart(blockState.getBlock());
	}
	
	
	//Branches
	
	public static boolean isBranch(Block block) {
		return block instanceof BranchBlock;//Oh shuddap you java purists.. this is minecraft!
	}

	public static boolean isBranch(@Nullable final BlockState state) {
		return state != null && isBranch(state.getBlock());
	}

	@Nullable
	public static BranchBlock getBranch(Block block) {
		return isBranch(block) ? (BranchBlock)block : null;
	}

	@Nullable
	public static BranchBlock getBranch(ITreePart treepart) {
		return treepart instanceof BranchBlock ? (BranchBlock)treepart : null;
	}

	@Nullable
	public static BranchBlock getBranch(BlockState state) {
		return getBranch(state.getBlock());
	}
	
	public static int getRadius(IBlockReader access, BlockPos pos) {
		BlockState state = access.getBlockState(pos);
		return getTreePart(state).getRadius(state);
	}
	
	public static Optional<BranchBlock> getBranchOpt(Block block) {
		return isBranch(block) ? Optional.of((BranchBlock)block) : Optional.empty();
	}
	
	public static Optional<BranchBlock> getBranchOpt(BlockState state) {
		return isBranch(state.getBlock()) ? Optional.of((BranchBlock)state.getBlock()) : Optional.empty();
	}
	
	public static Optional<BranchBlock> getBranchOpt(ITreePart treepart) {
		return treepart instanceof BranchBlock ? Optional.of((BranchBlock)treepart) : Optional.empty();
	}
	
	
	//Leaves
	
	public static boolean isLeaves(Block block) {
		return block instanceof DynamicLeavesBlock;
	}
	
	public static boolean isLeaves(BlockState blockState) {
		return isLeaves(blockState.getBlock());
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(Block block) {
		return isLeaves(block) ? (DynamicLeavesBlock)block : null;
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(ITreePart treepart) {
		return treepart instanceof DynamicLeavesBlock ? (DynamicLeavesBlock)treepart : null;
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(BlockState state) {
		return getLeaves(state.getBlock());
	}
	
	//Rooty
	
	public static boolean isRooty(Block block) {
		return block instanceof RootyBlock;
	}
	
	public static boolean isRooty(BlockState blockState) {
		return isRooty(blockState.getBlock());
	}

	@Nullable
	public static RootyBlock getRooty(Block block) {
		return isRooty(block) ? (RootyBlock)block : null;
	}

	@Nullable
	public static RootyBlock getRooty(ITreePart treepart) {
		return treepart instanceof RootyBlock ? (RootyBlock)treepart : null;
	}

	@Nullable
	public static RootyBlock getRooty(BlockState blockState) {
		return getRooty(blockState.getBlock());
	}
	
}
