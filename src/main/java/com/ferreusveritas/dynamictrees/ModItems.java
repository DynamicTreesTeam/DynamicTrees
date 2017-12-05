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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
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
	
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
		//Item Blocks
		registerItemBlock(registry, ModBlocks.blockRootyDirt);
		registerItemBlock(registry, ModBlocks.blockBonsaiPot);
		
		registry.register(ModItems.dendroPotion);
		registry.register(ModItems.dirtBucket);
		registry.register(ModItems.treeStaff);
		
		ArrayList<Item> treeItems = new ArrayList<Item>();
		ArrayList<Block> treeBlocks = new ArrayList<Block>();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.getRegisterableItems(treeItems);
			tree.getRegisterableBlocks(treeBlocks);
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
		
		DynamicTrees.compatProxy.registerItems(event);
	}

	public static void registerItemBlock(final IForgeRegistry<Item> registry, Block block) {
		registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}
	
}
