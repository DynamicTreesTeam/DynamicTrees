package com.ferreusveritas.dynamictrees.api;

import java.util.HashMap;

import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ISpecies;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.NullTreePart;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TreeHelper {

	private static HashMap<String, HashMap<Integer, BlockDynamicLeaves> > modLeavesArray = new HashMap<String, HashMap<Integer, BlockDynamicLeaves>>();
	
	public static final ITreePart nullTreePart = new NullTreePart();

	/**
	 * A convenience function for packing 4 {@link BlockDynamicLeaves} blocks into one Minecraft block using metadata.
	 * 
	 * @param modid
	 * @param seq
	 * @return
	 */
	public static BlockDynamicLeaves getLeavesBlockForSequence(String modid, int seq) {

		HashMap<Integer, BlockDynamicLeaves> leavesMap = getLeavesMapForModId(modid);
		int leavesBlockNum = seq / 4;
		int key = leavesBlockNum;
		
		if(leavesMap.containsKey(key)) {
			return leavesMap.get(key);
		} else {
			BlockDynamicLeaves leavesBlock = new BlockDynamicLeaves();
			leavesBlock.setRegistryName(modid, "leaves" + leavesBlockNum);
			leavesBlock.setUnlocalizedName("leaves" + leavesBlockNum);
			leavesMap.put(key, leavesBlock);
			return leavesBlock;
		}
	}
	
	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 * 
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link BlockDynamicLeaves}
	 */
	public static HashMap<Integer, BlockDynamicLeaves> getLeavesMapForModId(String modid) {
		HashMap<Integer, BlockDynamicLeaves> leavesMap;
		
		if(modLeavesArray.containsKey(modid)) {
			leavesMap = modLeavesArray.get(modid);
		} else {
			leavesMap = new HashMap<Integer, BlockDynamicLeaves>();
			modLeavesArray.put(modid, leavesMap);
		}

		return leavesMap;
	}
	
	/**
	 * Convenience method to pulse a single growth cycle and age the cuboid volume.
	 * Used by the growth potion and the dendrocoil.
	 * 
	 * @param world
	 * @param pos
	 */
	public static void growPulse(World world, BlockPos pos) {
		Block dirtCandidate = world.getBlockState(pos).getBlock();
		if(dirtCandidate instanceof BlockRootyDirt) {
			BlockRootyDirt dirt = (BlockRootyDirt) dirtCandidate;	
			dirt.grow(world, pos, world.rand);
			ageVolume(world, pos, 1);
		}
	}
	
	/** 
	 * Shortcut to blindly age a cuboid volume.
	 * 
	 * @param world
	 * @param pos The position of the bottom most block of a trees trunk
	 */
	public static void ageVolume(World world, BlockPos pos, int interations) {
		ageVolume(world, pos, 8, 32, null, interations);
	}
	
	/**
	 * Pulses an entire cuboid volume of blocks each with an age signal.
	 * Warning: CPU intensive and should be used sparingly
	 * 
	 * @param world The world
	 * @param pos The position of the bottom most block of a trees trunk
	 * @param halfWidth The "radius" of the cuboid volume
	 * @param height The height of the cuboid volume
	 */
	public static void ageVolume(World world, BlockPos pos, int halfWidth, int height, SimpleVoxmap leafMap, int iterations){
		
		Iterable<BlockPos> iterable = leafMap != null ? leafMap.getAllNonZero() : 
			BlockPos.getAllInBox(pos.add(new BlockPos(-halfWidth, 0, -halfWidth)), pos.add(new BlockPos(halfWidth, height, halfWidth)));
		
		for(int i = 0; i < iterations; i++) {
			for(BlockPos iPos: iterable) {
				IBlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if(block instanceof IAgeable) {
					((IAgeable)block).age(world, iPos, blockState, world.rand, true);
				}
			}
		}
		
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
	public static ISpecies getExactSpecies(World world, BlockPos pos) {
		
		IBlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
		
		BlockPos rootPos = pos;
		ISpecies commonSpecies = null;
		
		if(isBranch(block)) {
			BlockBranch branch = (BlockBranch) block;
			commonSpecies = branch.getTree().getCommonSpecies();
			MapSignal signal = branch.analyse(world, pos, null, new MapSignal());// Analyze entire tree network to find root node
			
			if(signal.found) {// Root node was found
				rootPos = signal.root;
				block = world.getBlockState(rootPos).getBlock();
			}
		}
		
		if(isRootyDirt(block)) {
			return ((BlockRootyDirt) block).getSpecies(world, rootPos);
		}
		
		return commonSpecies;
	}
	
	
	public static String getSpeciesFullName(ISpecies species) {
		if(species != null) {
			return species.getModId() + ":" + species.getName();
		}
		
		return "";
	}
	
	//Treeparts

	public static boolean isTreePart(Block block) {
		return block instanceof ITreePart;
	}

	public static boolean isTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return isTreePart(blockAccess.getBlockState(pos).getBlock());
	}

	public static ITreePart getTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : null;
	}

	public static ITreePart getTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return getTreePart(blockAccess.getBlockState(pos).getBlock());
	}

	public static ITreePart getTreePart(IBlockState state) {
		return getTreePart(state.getBlock());
	}

	public static ITreePart getSafeTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : nullTreePart;
	}

	public static ITreePart getSafeTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return getSafeTreePart(blockAccess.getBlockState(pos));
	}

	public static ITreePart getSafeTreePart(IBlockState blockState) {
		return getSafeTreePart(blockState.getBlock());
	}

	//Branches

	public static boolean isBranch(Block block) {
		return block instanceof BlockBranch;//Oh shuddap you java purists.. this is minecraft!
	}

	public static boolean isBranch(IBlockAccess blockAccess, BlockPos pos) {
		return isBranch(blockAccess.getBlockState(pos).getBlock());
	}

	public static BlockBranch getBranch(Block block) {
		return isBranch(block) ? (BlockBranch)block : null;
	}

	public static BlockBranch getBranch(ITreePart treepart) {
		return treepart instanceof BlockBranch ? (BlockBranch)treepart : null;
	}

	public static BlockBranch getBranch(IBlockAccess blockAccess, BlockPos pos) {
		return getBranch(blockAccess.getBlockState(pos).getBlock());
	}

	//Leaves

	public static boolean isLeaves(Block block) {
		return block instanceof BlockDynamicLeaves;
	}

	public static boolean isLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return isLeaves(blockAccess.getBlockState(pos).getBlock());
	}

	public static boolean isLeaves(IBlockState blockState) {
		return isLeaves(blockState.getBlock());
	}
	
	//Rooty Dirt

	public static boolean isRootyDirt(Block block) {
		return block instanceof BlockRootyDirt;
	}

	public static boolean isRootyDirt(IBlockAccess blockAccess, BlockPos pos) {
		return isRootyDirt(blockAccess.getBlockState(pos).getBlock());
	}

}
