package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

///////////////////////////////////////////
// REGISTRATION
///////////////////////////////////////////

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class RegistrationEventHandler {

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(ModBlocks.blockRootyDirt);
		registry.register(ModBlocks.blockDynamicSapling);
		registry.register(ModBlocks.blockBonsaiPot);
		registry.register(ModBlocks.blockFruitCocoa);

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.registerBlocks(registry);
		}

		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			registry.register(leavesBlock);
		}

		DynamicTrees.compatProxy.registerBlocks(event);
	}
	
	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();

		registry.register(ModItems.treeStaff);

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.registerItems(registry);
		}

		registry.register(ModItems.dendroPotion);
		registry.register(ModItems.dirtBucket);
		
		for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			registry.register(new ItemBlock(leavesBlock).setRegistryName(leavesBlock.getRegistryName()));
		}

		ItemBlock itemBlock = new ItemBlock(ModBlocks.blockRootyDirt);
		itemBlock.setRegistryName(ModBlocks.blockRootyDirt.getRegistryName());
		registry.register(itemBlock);

		ItemBlock itemBonsaiBlock = new ItemBlock(ModBlocks.blockBonsaiPot);
		itemBonsaiBlock.setRegistryName(ModBlocks.blockBonsaiPot.getRegistryName());
		registry.register(itemBonsaiBlock);
		
		DynamicTrees.compatProxy.registerItems(event);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
		final IForgeRegistry<IRecipe> registry = event.getRegistry();

		for(DynamicTree tree: ModTrees.baseTrees) {
			tree.registerRecipes(registry);
		}

		ModItems.dirtBucket.registerRecipes(registry);			
		ModItems.dendroPotion.registerRecipes(registry);
		
		DynamicTrees.compatProxy.registerRecipes(event);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		DynamicTrees.proxy.registerModels();
	}
}