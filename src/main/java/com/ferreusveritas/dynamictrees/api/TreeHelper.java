package com.ferreusveritas.dynamictrees.api;

import java.util.HashMap;

import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.NullTreePart;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeTwinkle;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TreeHelper {
	
	private static HashMap<String, HashMap<Integer, BlockDynamicLeaves> > modLeavesArray = new HashMap<String, HashMap<Integer, BlockDynamicLeaves>>();
	
	public static final ITreePart nullTreePart = new NullTreePart();
	
	
	public static BlockDynamicLeaves getLeavesBlockForSequence(String modid, int seq, ILeavesProperties leavesProperties) {
		BlockDynamicLeaves leaves = getLeavesBlockForSequence(modid, seq);
		leavesProperties.setDynamicLeavesState(leaves.getDefaultState().withProperty(BlockDynamicLeaves.TREE, seq & 3));
		return leaves;
	}
	
	/**
	 * A convenience function for packing 4 {@link BlockDynamicLeaves} blocks into one Minecraft block using metadata.
	 * 
	 * @param modid
	 * @param seq
	 * @return
	 */
	public static BlockDynamicLeaves getLeavesBlockForSequence(String modid, int seq) {
		int key = seq / 4;
		String regname = "leaves" + key;
		
		return getLeavesMapForModId(modid).computeIfAbsent(key, k -> (BlockDynamicLeaves)new BlockDynamicLeaves().setRegistryName(modid, regname).setUnlocalizedName(regname));
	}
	
	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 * 
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link BlockDynamicLeaves}
	 */
	public static HashMap<Integer, BlockDynamicLeaves> getLeavesMapForModId(String modid) {
		return modLeavesArray.computeIfAbsent(modid, k -> new HashMap<Integer, BlockDynamicLeaves>());
	}
	
	/**
	 * Convenience method to pulse a single growth cycle and age the cuboid volume.
	 * Used by the growth potion and the dendrocoil.
	 * 
	 * @param world
	 * @param rootPos
	 */
	public static void growPulse(World world, BlockPos rootPos) {
		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);
		if(dirt != null) {
			dirt.updateTree(world, rootPos, world.rand, true);
			ageVolume(world, rootPos, 1);
		}
	}
	
	/** 
	 * Shortcut to blindly age a cuboid volume.
	 * 
	 * @param world
	 * @param pos The position of the bottom most block of a trees trunk
	 */
	public static void ageVolume(World world, BlockPos pos, int iterations) {
		ageVolume(world, pos, 8, 32, null, iterations);
	}
	
	/**
	 * Pulses an entire cuboid volume of blocks each with an age signal.
	 * Warning: CPU intensive and should be used sparingly
	 * 
	 * @param world The world
	 * @param treePos The position of the bottom most block of a trees trunk
	 * @param halfWidth The "radius" of the cuboid volume
	 * @param height The height of the cuboid volume
	 */
	public static void ageVolume(World world, BlockPos treePos, int halfWidth, int height, SimpleVoxmap leafMap, int iterations){
		
		Iterable<BlockPos> iterable = leafMap != null ? leafMap.getAllNonZero((byte) 0x0F) : 
			BlockPos.getAllInBox(treePos.add(new BlockPos(-halfWidth, 0, -halfWidth)), treePos.add(new BlockPos(halfWidth, height, halfWidth)));
		
		for(int i = 0; i < iterations; i++) {
			for(BlockPos iPos: iterable) {
				IBlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if(block instanceof IAgeable) {
					if(((IAgeable)block).age(world, iPos, blockState, world.rand, true)) {
						if(leafMap != null) {
							leafMap.setVoxel(iPos, (byte) 0);
						}
					}
				}
			}
		}
		
	}
	
	public static void treeParticles(World world, BlockPos rootPos, EnumParticleTypes type, int num) {
		if(world.isRemote) {
			startAnalysisFromRoot(world, rootPos, new MapSignal(new NodeTwinkle(type, num)));
		}
	}

	public static boolean startAnalysisFromRoot(World world, BlockPos rootPos, MapSignal signal) {
		BlockRootyDirt dirt = TreeHelper.getRootyDirt(world, rootPos);
		if(dirt != null) {
			dirt.startAnalysis(world, rootPos, signal);
			return true;
		}
		return false;
	}
	
	//Treeparts
	
	public final static boolean isTreePart(Block block) {
		return block instanceof ITreePart;
	}
	
	public final static boolean isTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return isTreePart(blockAccess.getBlockState(pos).getBlock());
	}
	
	public final static ITreePart getTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : nullTreePart;
	}
	
	public final static ITreePart getTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return getTreePart(blockAccess.getBlockState(pos));
	}
	
	public final static ITreePart getTreePart(IBlockState blockState) {
		return getTreePart(blockState.getBlock());
	}
	
	
	//Branches
	
	public final static boolean isBranch(Block block) {
		return block instanceof BlockBranch;//Oh shuddap you java purists.. this is minecraft!
	}
	
	public final static boolean isBranch(IBlockAccess blockAccess, BlockPos pos) {
		return isBranch(blockAccess.getBlockState(pos).getBlock());
	}
	
	public final static boolean isBranch(IBlockState state) {
		return isBranch(state.getBlock());
	}
	
	public final static BlockBranch getBranch(Block block) {
		return isBranch(block) ? (BlockBranch)block : null;
	}
	
	public final static BlockBranch getBranch(ITreePart treepart) {
		return treepart instanceof BlockBranch ? (BlockBranch)treepart : null;
	}
	
	public final static BlockBranch getBranch(IBlockAccess blockAccess, BlockPos pos) {
		return getBranch(blockAccess.getBlockState(pos));
	}
	
	public final static BlockBranch getBranch(IBlockState state) {
		return getBranch(state.getBlock());
	}
	
	//Leaves
	
	public final static boolean isLeaves(Block block) {
		return block instanceof BlockDynamicLeaves;
	}
	
	public final static boolean isLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return isLeaves(blockAccess.getBlockState(pos).getBlock());
	}
	
	public final static boolean isLeaves(IBlockState blockState) {
		return isLeaves(blockState.getBlock());
	}
	
	public final static BlockDynamicLeaves getDynamicLeaves(Block block) {
		return isLeaves(block) ? (BlockDynamicLeaves)block : null;
	}
	
	public final static BlockDynamicLeaves getDynamicLeaves(ITreePart treepart) {
		return treepart instanceof BlockDynamicLeaves ? (BlockDynamicLeaves)treepart : null;
	}
	
	public final static BlockDynamicLeaves getDynamicLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return getDynamicLeaves(blockAccess.getBlockState(pos));
	}
	
	public final static BlockDynamicLeaves getDynamicLeaves(IBlockState state) {
		return getDynamicLeaves(state.getBlock());
	}
	
	//Rooty Dirt
	
	public final static boolean isRootyDirt(Block block) {
		return block instanceof BlockRootyDirt;
	}
	
	public final static boolean isRootyDirt(IBlockState soilBlockState) {
		return isRootyDirt(soilBlockState.getBlock());
	}
	
	public final static boolean isRootyDirt(IBlockAccess blockAccess, BlockPos pos) {
		return isRootyDirt(blockAccess.getBlockState(pos));
	}
	
	public final static BlockRootyDirt getRootyDirt(Block block) {
		return isRootyDirt(block) ? (BlockRootyDirt)block : null;
	}
	
	public final static BlockRootyDirt getRootyDirt(IBlockAccess blockAccess, BlockPos pos) {
		return getRootyDirt(blockAccess.getBlockState(pos).getBlock());
	}
	
}
