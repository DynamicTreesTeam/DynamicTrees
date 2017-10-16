package com.ferreusveritas.dynamictrees.api;

import java.util.HashMap;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.NullTreePart;

import net.minecraft.block.Block;
import com.ferreusveritas.dynamictrees.util.Dir;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TreeHelper {

	public static final short cellSolverDeciduous[] = {0x0514, 0x0423, 0x0322, 0x0411, 0x0311, 0x0211};
	public static final short cellSolverConifer[] = {0x0514, 0x0413, 0x0312, 0x0211};
	public static final short hydroSolverDeciduous[] = null;
	public static final short hydroSolverConifer[] = {0x02F0, 0x0144, 0x0742, 0x0132, 0x0730};

	public static HashMap<String, BlockGrowingLeaves> leavesArray = new HashMap<String, BlockGrowingLeaves>();

	public static ITreePart nullTreePart = new NullTreePart();

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
			leavesBlock.setRegistryName("leaves" + leavesBlockNum);
			leavesBlock.setUnlocalizedNameReg("leaves" + leavesBlockNum);
			leavesArray.put(key, leavesBlock);
			return leavesBlock;
		}
	}

	public static boolean isSurroundedByExistingChunks(World world, BlockPos pos) {
		for(Dir d: Dir.SURROUND) {
			if(!world.getChunkProvider().chunkExists((pos.getX() >> 4) + d.xOffset, (pos.getZ() >> 4) + d.zOffset)) {
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
		Block dirtCandidate = pos.getBlock(world);
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
			Block block = iPos.getBlock(world);
			if(block instanceof IAgeable) {
				((IAgeable)block).age(world, iPos, world.rand, true);
			}
		}
	}
	
	//Treeparts

	public static boolean isTreePart(Block block) {
		return block instanceof ITreePart;
	}

	public static boolean isTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return isTreePart(pos.getBlock(blockAccess));
	}

	public static ITreePart getTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : null;
	}

	public static ITreePart getTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return getTreePart(pos.getBlock(blockAccess));
	}

	public static ITreePart getSafeTreePart(Block block) {
		return isTreePart(block)? (ITreePart)block : nullTreePart;
	}

	public static ITreePart getSafeTreePart(IBlockAccess blockAccess, BlockPos pos) {
		return getSafeTreePart(pos.getBlock(blockAccess));
	}

	//Branches

	public static boolean isBranch(Block block) {
		return block instanceof BlockBranch;//Oh shuddap you java purists.. this is minecraft!
	}

	public static boolean isBranch(IBlockAccess blockAccess, BlockPos pos) {
		return isBranch(pos.getBlock(blockAccess));
	}

	public static BlockBranch getBranch(Block block) {
		return isBranch(block) ? (BlockBranch)block : null;
	}

	public static BlockBranch getBranch(ITreePart treepart) {
		return treepart instanceof BlockBranch ? (BlockBranch)treepart : null;
	}

	public static BlockBranch getBranch(IBlockAccess blockAccess, BlockPos pos) {
		return getBranch(pos.getBlock(blockAccess));
	}

	//Leaves

	public static boolean isLeaves(Block block) {
		return block instanceof BlockGrowingLeaves;
	}

	public static boolean isLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return isLeaves(pos.getBlock(blockAccess));
	}

	//Rooty Dirt

	public static boolean isRootyDirt(Block block) {
		return block instanceof BlockRootyDirt;
	}

	public static boolean isRootyDirt(IBlockAccess blockAccess, BlockPos pos) {
		return isRootyDirt(pos.getBlock(blockAccess));
	}

}
