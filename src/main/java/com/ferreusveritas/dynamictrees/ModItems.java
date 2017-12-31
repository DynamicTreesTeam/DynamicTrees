package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {
	
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static Staff treeStaff;
	
	public static void preInit() {
		dendroPotion = new DendroPotion();//Potions
		dirtBucket = new DirtBucket();//Dirt Bucket
		treeStaff = new Staff();//Creative Mode Staff
	}
	
	public static void registerItems(IForgeRegistry<Item> registry) {
		ArrayList<Item> treeItems = new ArrayList<Item>();
		ModTrees.baseTrees.forEach(tree -> tree.getRegisterableItems(treeItems));
		TreeHelper.getLeavesMapForModId(ModConstants.MODID).forEach((key, block) -> treeItems.add(makeItemBlock(block)));
		
		registry.registerAll(makeItemBlock(ModBlocks.blockRootyDirt), makeItemBlock(ModBlocks.blockBonsaiPot));
		registry.registerAll(dendroPotion, dirtBucket, treeStaff);
		registry.registerAll(treeItems.toArray(new Item[0]));
		
		DynamicTrees.compatProxy.registerItems(registry);
	}

	public static Item makeItemBlock(Block block) {
		return new ItemBlock(block).setRegistryName(block.getRegistryName());
	}
	
	public static void registerItemBlock(final IForgeRegistry<Item> registry, Block block) {
		registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}
	
}
