package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ModRecipes {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes() {

		ModItems.dendroPotion.registerRecipes();
		ModItems.dirtBucket.registerRecipes();
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSapling();

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

		DynamicTrees.compatProxy.registerRecipes();
	}
	
}
