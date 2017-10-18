package com.ferreusveritas.dynamictrees.api;

import java.util.HashMap;

import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.NullTreePart;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TreeHelper {

	public static HashMap<String, BlockGrowingLeaves> leavesArray = new HashMap<String, BlockGrowingLeaves>();

	public static final ITreePart nullTreePart = new NullTreePart();

	/**
	 * A convenience function for packing 4 growing leaves blocks into one Minecraft block using metadata.
	 * 
	 * @param modid
	 * @param seq
	 * @return
	 */
	public static BlockGrowingLeaves getLeavesBlockForSequence(String modid, int seq) {
		int leavesBlockNum = seq / 4;
		String key = modid + ":" + leavesBlockNum;

		if(leavesArray.containsKey(key)) {
			return leavesArray.get(key);
		} else {
			BlockGrowingLeaves leavesBlock = new BlockGrowingLeaves();
			leavesBlock.setRegistryName(modid, "leaves" + leavesBlockNum);
			leavesBlock.setUnlocalizedName("leaves" + leavesBlockNum);
			leavesArray.put(key, leavesBlock);
			return leavesBlock;
		}
	}

	public static boolean isSurroundedByExistingChunks(World world, BlockPos pos) {
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			if(world.getChunkProvider().getLoadedChunk((pos.getX() >> 4) + dir.getFrontOffsetX(), (pos.getZ() >> 4) + dir.getFrontOffsetZ()) == null ){
				return false;
			}
		}
		
		return true;
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
			ageVolume(world, pos);
		}
	}
	
	/** 
	 * Shortcut to blindly age a cuboid volume.
	 * 
	 * @param world
	 * @param pos The position of the bottom most block of a trees trunk
	 */
	public static void ageVolume(World world, BlockPos pos) {
		ageVolume(world, pos, 8, 32, null);
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
	public static void ageVolume(World world, BlockPos pos, int halfWidth, int height, SimpleVoxmap leafMap){
		
		Iterable<BlockPos> iterable = leafMap != null ? leafMap.getAllNonZero() : 
			BlockPos.getAllInBox(pos.add(new BlockPos(-halfWidth, 0, -halfWidth)), pos.add(new BlockPos(halfWidth, height, halfWidth)));
		
		for(BlockPos iPos: iterable) {
			IBlockState blockState = world.getBlockState(iPos);
			Block block = blockState.getBlock();
			if(block instanceof IAgeable) {
				((IAgeable)block).age(world, iPos, blockState, world.rand, true);
			}
		}
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
		return block instanceof BlockGrowingLeaves;
	}

	public static boolean isLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return isLeaves(blockAccess.getBlockState(pos).getBlock());
	}

	//Rooty Dirt

	public static boolean isRootyDirt(Block block) {
		return block instanceof BlockRootyDirt;
	}

	public static boolean isRootyDirt(IBlockAccess blockAccess, BlockPos pos) {
		return isRootyDirt(blockAccess.getBlockState(pos).getBlock());
	}

}
