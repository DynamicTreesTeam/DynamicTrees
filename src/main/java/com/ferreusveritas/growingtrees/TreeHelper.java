package com.ferreusveritas.growingtrees;

import java.util.HashMap;

import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.growingtrees.blocks.BlockRootyDirt;
import com.ferreusveritas.growingtrees.blocks.ITreePart;
import com.ferreusveritas.growingtrees.blocks.NullTreePart;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class TreeHelper {
	
	public static final short cellSolverDeciduous[] = {0x0514, 0x0423, 0x0322, 0x0411, 0x0311, 0x0211};
	public static final short cellSolverConifer[] = {0x0514, 0x0413, 0x0312, 0x0211};
	public static final short hydroSolverDeciduous[] = null;
	public static final short hydroSolverConifer[] = {0x02F0, 0x0144, 0x0742, 0x0132, 0x0730};
	
	public static HashMap<String, BlockGrowingLeaves> leavesArray = new HashMap<String, BlockGrowingLeaves>();
	
	public static ITreePart nullTreePart = new NullTreePart();
	
	public static BlockGrowingLeaves getLeavesBlockForSequence(String modid, String name, int seq){
		int leavesBlockNum = seq / 4;
		String key = modid + ":" + leavesBlockNum;
		
		if(leavesArray.containsKey(key)){
			return leavesArray.get(key);
		} else {
			BlockGrowingLeaves leavesBlock = new BlockGrowingLeaves();
			leavesBlock.setBlockName(modid + "_" + "leaves" + leavesBlockNum);
			GameRegistry.registerBlock(leavesBlock, "leaves" + leavesBlockNum);
			leavesArray.put(key, leavesBlock);
			return leavesBlock;
		}
	}
	
	//Treeparts

	public static boolean isTreePart(Block block){
		return block instanceof ITreePart;
	}

	public static boolean isTreePart(IBlockAccess blockAccess, int x, int y, int z){
		return isTreePart(blockAccess.getBlock(x, y, z));
	}
	
    public static ITreePart getTreePart(Block block){
    	return isTreePart(block)? (ITreePart)block : null;
    }
    
    public static ITreePart getTreePart(IBlockAccess blockAccess, int x, int y, int z){
    	return getTreePart(blockAccess.getBlock(x, y, z));
    }

    public static ITreePart getSafeTreePart(Block block){
    	return isTreePart(block)? (ITreePart)block : nullTreePart;
    }

    public static ITreePart getSafeTreePart(IBlockAccess blockAccess, int x, int y, int z){
    	return getSafeTreePart(blockAccess.getBlock(x, y, z));
    }
    
	//Branches
	
	public static boolean isBranch(Block block){
		return block instanceof BlockBranch;//Oh shuddap you java purists.. this is minecraft!
	}

	public static boolean isBranch(IBlockAccess blockAccess, int x, int y, int z){
		return isBranch(blockAccess.getBlock(x, y, z));
	}
	
	public static BlockBranch getBranch(Block block){
		return isBranch(block) ? (BlockBranch)block : null;
	}

	public static BlockBranch getBranch(ITreePart treepart){
		return treepart instanceof BlockBranch ? (BlockBranch)treepart : null;
	}
	
	public static BlockBranch getBranch(IBlockAccess blockAccess, int x, int y, int z){
		return getBranch(blockAccess.getBlock(x, y, z));
	}
	
	//Leaves
	
	public static boolean isLeaves(Block block){
		return block instanceof BlockGrowingLeaves;
	}
	
	public static boolean isLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return isLeaves(blockAccess.getBlock(x, y, z));
	}
	
	//Rooty Dirt
	
	public static boolean isRootyDirt(Block block){
		return block instanceof BlockRootyDirt;//
	}
	
	public static boolean isRootyDirt(IBlockAccess blockAccess, int x, int y, int z){
		return isRootyDirt(blockAccess.getBlock(x, y, z));
	}
	
}
