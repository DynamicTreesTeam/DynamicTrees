package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ModRecipes {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes() {

		ModItems.dendroPotion.registerRecipes();
		
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.dirtBucket), new Object[]{ Blocks.DIRT, Items.BUCKET});
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSaplingBlockState();

			if(primitiveSapling != null) {
				ItemStack saplingStack = new ItemStack(primitiveSapling.getBlock());
				saplingStack.setItemDamage(primitiveSapling.getValue(BlockSapling.TYPE).getMetadata());

				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				
				//Create a seed from a sapling and dirt bucket
				GameRegistry.addShapelessRecipe(seedStack, new Object[]{ saplingStack, ModItems.dirtBucket});

				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(saplingStack, new Object[]{ seedStack, ModItems.dirtBucket });

				//Register the seed in the ore dictionary as a sapling since we can convert for free anyway.
				OreDictionary.registerOre("treeSapling", seedStack);
			}

		}
		
		//Create an apple seed from an apple and dirt bucket
		if(ModConfigs.enableAppleTrees) {
			GameRegistry.addShapelessRecipe(Species.REGISTRY.getValue(new ResourceLocation(ModConstants.MODID, "apple")).getSeedStack(1), new Object[]{ new ItemStack(Items.APPLE), ModItems.dirtBucket});
		}
		
		DynamicTrees.compatProxy.registerRecipes();
	}
	
}
