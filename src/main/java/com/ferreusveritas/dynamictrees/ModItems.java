package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;

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
	
	public static void register(IForgeRegistry<Item> registry) {
		ArrayList<Item> treeItems = new ArrayList<Item>();
		ModTrees.baseFamilies.forEach(tree -> tree.getRegisterableItems(treeItems));
		ModTrees.dynamicCactus.getRegisterableItems(treeItems);
		
		registry.registerAll(dendroPotion, dirtBucket, treeStaff);
		registry.registerAll(treeItems.toArray(new Item[0]));
		
		registry.register(new ItemBlock(ModBlocks.experimental).setRegistryName(ModBlocks.experimental.getRegistryName()));
	}
	
}
