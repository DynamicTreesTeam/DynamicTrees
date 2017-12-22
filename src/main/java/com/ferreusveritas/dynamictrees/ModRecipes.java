package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
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
		ModItems.dirtBucket.registerRecipes(registry);
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSapling();

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

		DynamicTrees.compatProxy.registerRecipes(registry);
	}
	
}
