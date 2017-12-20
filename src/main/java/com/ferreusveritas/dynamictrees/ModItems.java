package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.api.backport.ItemBackport;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;

public class ModItems {
	
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static Staff treeStaff;
	
	public static void preInit() {
		
		//Potions
		dendroPotion = new DendroPotion();
		
		//Dirt Bucket
		dirtBucket = new DirtBucket();
		
		//Creative Mode Staff
		treeStaff = new Staff();
	}
	
	public static void registerItems() {
		
		//Item Blocks
		registerItemBlock(ModBlocks.blockRootyDirt);
		registerItemBlock(ModBlocks.blockBonsaiPot);
		
		GameRegistry.register(ModItems.dendroPotion);
		GameRegistry.register(ModItems.dirtBucket);
		GameRegistry.register(ModItems.treeStaff);
		
		ArrayList<BlockBackport> treeBlocks = new ArrayList<BlockBackport>();
		ArrayList<ItemBackport> treeItems = new ArrayList<ItemBackport>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
			tree.getRegisterableItems(treeItems);
		}

		for(ItemBackport item: treeItems) {
			GameRegistry.register(item);
		}
		
		for(Block block: treeBlocks) {
			registerItemBlock(block);
		}
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			registerItemBlock(leavesBlock);
		}
		
		DynamicTrees.compatProxy.registerItems();
	}

	public static void registerItemBlock(Block block) {
		//GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}
	
}
