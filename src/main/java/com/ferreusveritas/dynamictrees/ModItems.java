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
import net.minecraftforge.registries.IForgeRegistry;

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
	
	public static void registerItems(IForgeRegistry<Item> registry) {
		
		//Item Blocks
		registerItemBlock(registry, ModBlocks.blockRootyDirt);
		registerItemBlock(registry, ModBlocks.blockBonsaiPot);
		
		registry.register(ModItems.dendroPotion);
		registry.register(ModItems.dirtBucket);
		registry.register(ModItems.treeStaff);
		
		ArrayList<Block> treeBlocks = new ArrayList<Block>();
		ArrayList<Item> treeItems = new ArrayList<Item>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableBlocks(treeBlocks);
			registry.register(tree.getCommonSpecies().getSeed());
		}

		for(Item item: treeItems) {
			registry.register(item);
		}
		
		for(Block block: treeBlocks) {
			registerItemBlock(registry, block);
		}
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			registerItemBlock(registry, leavesBlock);
		}
		
		DynamicTrees.compatProxy.registerItems(registry);
	}

	public static void registerItemBlock(final IForgeRegistry<Item> registry, Block block) {
		registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}
	
}
