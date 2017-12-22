package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockVerboseFire;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.init.Blocks;

public class ModBlocks {
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	public static BlockVerboseFire blockVerboseFire;
	
	public static CommonBlockStates blockStates;
	
	public static void preInit() {
		
		blockStates = new CommonBlockStates();
		
		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		
		//Dynamic version of a Vanilla sapling
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");
		
		//Bonsai Pot
		blockBonsaiPot = new BlockBonsaiPot();
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
	
		//Verbose Fire
		blockVerboseFire = new BlockVerboseFire();
	}
	
	public static void registerBlocks() {

		GameRegistry.register(ModBlocks.blockRootyDirt);
		GameRegistry.register(ModBlocks.blockDynamicSapling);
		GameRegistry.register(ModBlocks.blockBonsaiPot);
		GameRegistry.register(ModBlocks.blockFruitCocoa);
		GameRegistry.register(ModBlocks.blockVerboseFire);

		ArrayList<BlockBackport> treeBlocks = new ArrayList<BlockBackport>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
		}

		for(BlockBackport block: treeBlocks) {
			GameRegistry.register(block);
		}
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			GameRegistry.register(leavesBlock);
		}

		DynamicTrees.compatProxy.registerBlocks();
	}

	public static class CommonBlockStates {
		public final IBlockState dirt;
		public final IBlockState podzol;
		public final IBlockState redMushroom;
		public final IBlockState brownMushroom;
		
		public CommonBlockStates() {
			dirt = new BlockState(Blocks.dirt, 0);
			podzol = new BlockState(Blocks.dirt, 2);
			redMushroom = new BlockState(Blocks.red_mushroom);
			brownMushroom = new BlockState(Blocks.brown_mushroom);
		}
	}
	
}
