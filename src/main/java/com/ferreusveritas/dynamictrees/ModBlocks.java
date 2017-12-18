package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.blocks.BlockVerboseFire;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	public static BlockVerboseFire blockVerboseFire;
	
	public static void preInit() {
		
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

		ArrayList<Block> treeBlocks = new ArrayList<Block>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
		}

		for(Block block: treeBlocks) {
			GameRegistry.register(block);
		}
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			GameRegistry.register(leavesBlock);
		}

		DynamicTrees.compatProxy.registerBlocks();
	}
	
}
