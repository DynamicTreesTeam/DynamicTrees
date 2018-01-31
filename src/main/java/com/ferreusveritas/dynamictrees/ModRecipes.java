package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipes {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes(IForgeRegistry<IRecipe> registry) {

		ModItems.dendroPotion.registerRecipes(registry);
		
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ResourceLocation(ModConstants.MODID, "dirtbucket"), null, new ItemStack(ModItems.dirtBucket), 
				new Ingredient[]{ Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(ItemBlock.getItemFromBlock(Blocks.DIRT))});
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSaplingBlockState();

			if(primitiveSapling != null) {
				ItemStack saplingStack = new ItemStack(primitiveSapling.getBlock());
				saplingStack.setItemDamage(primitiveSapling.getValue(BlockSapling.TYPE).getMetadata());

				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				
				//Create a seed from a sapling and dirt bucket
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(ModConstants.MODID, tree.getName().getResourcePath() + "seed"),
					null,
					seedStack,
					new Ingredient[]{
						Ingredient.fromStacks(saplingStack),
						Ingredient.fromItem(ModItems.dirtBucket)
					}
				);

				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(ModConstants.MODID, tree.getName().getResourcePath() + "sapling"),
					null,
					saplingStack,
					new Ingredient[]{
						Ingredient.fromStacks(seedStack),
						Ingredient.fromItem(ModItems.dirtBucket)
					}
				);

				//Register the seed in the ore dictionary as a sapling since we can convert for free anyway.
				OreDictionary.registerOre("treeSapling", seedStack);
			}

		}
		
		//Create an apple seed from an apple and dirt bucket
		if(ModConfigs.enableAppleTrees) {
			GameRegistry.addShapelessRecipe(
				new ResourceLocation(ModConstants.MODID, "appleseed"),
				null,
				Species.REGISTRY.getValue(new ResourceLocation(ModConstants.MODID, "apple")).getSeedStack(1),
				new Ingredient[]{
						Ingredient.fromStacks(new ItemStack(Items.APPLE)),
						Ingredient.fromItem(ModItems.dirtBucket)
				}
			);
		}
		
		DynamicTrees.compatProxy.registerRecipes(registry);
	}
	
}
