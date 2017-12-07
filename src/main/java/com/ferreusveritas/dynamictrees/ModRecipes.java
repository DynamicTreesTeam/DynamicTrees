package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModRecipes {

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
		final IForgeRegistry<IRecipe> registry = event.getRegistry();

		ModItems.dendroPotion.registerRecipes(registry);
		ModItems.dirtBucket.registerRecipes(registry);
		
		for(DynamicTree tree: ModTrees.baseTrees) {

			IBlockState primitiveSapling = tree.getPrimitiveSapling();

			if(primitiveSapling != null) {
				//Creates a seed from a vanilla sapling and a wooden bowl
				ItemStack saplingStack = new ItemStack(primitiveSapling.getBlock());
				saplingStack.setItemDamage(primitiveSapling.getValue(BlockSapling.TYPE).getMetadata());

				ItemStack seedStack = tree.getCommonSpecies().getSeedStack(1);
				
				//Create a seed from a sapling and dirt bucket
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(ModConstants.MODID, tree.getName() + "seed"),
					null,
					seedStack,
					new Ingredient[]{
						Ingredient.fromStacks(saplingStack),
						Ingredient.fromItem(ModItems.dirtBucket)
					}
				);

				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(ModConstants.MODID, tree.getName() + "sapling"),
					null,
					saplingStack,
					new Ingredient[]{
						Ingredient.fromStacks(seedStack),
						Ingredient.fromItem(ModItems.dirtBucket)
					}
				);
			}

		}

		DynamicTrees.compatProxy.registerRecipes(event);
	}
	
}
