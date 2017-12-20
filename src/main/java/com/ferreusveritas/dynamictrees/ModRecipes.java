package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.item.ItemStack;

public class ModRecipes {

	public static void registerRecipes() {

		ModItems.dendroPotion.registerRecipes();
		ModItems.dirtBucket.registerRecipes();
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSapling();

			if(primitiveSapling != null) {
				//Creates a seed from a vanilla sapling and a wooden bowl
				ItemStack saplingStack = new ItemStack(primitiveSapling.getBlock());
				saplingStack.setItemDamage(primitiveSapling.getMeta());

				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				
				//Create a seed from a sapling and dirt bucket
				GameRegistry.addShapelessRecipe(seedStack, new Object[]{ saplingStack, ModItems.dirtBucket});

				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(saplingStack, new Object[]{ seedStack, ModItems.dirtBucket });

			}

		}

		DynamicTrees.compatProxy.registerRecipes();
	}
	
}
