package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		ArrayList<Item> treeItems = new ArrayList<Item>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
			tree.getRegisterableItems(treeItems);
		}

		for(Item item: treeItems) {
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
		GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}
	
}
