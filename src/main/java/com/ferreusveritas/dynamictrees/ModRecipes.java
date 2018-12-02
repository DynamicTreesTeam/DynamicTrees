package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockPlanks;
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
	public static void register(IForgeRegistry<IRecipe> registry) {

		ModItems.dendroPotion.registerRecipes(registry);
		
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ResourceLocation(ModConstants.MODID, "dirtbucket"), null, new ItemStack(ModItems.dirtBucket), 
				new Ingredient[]{ Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(ItemBlock.getItemFromBlock(Blocks.DIRT))});
		
		//Create a seed <-> sapling exchange for the 6 vanilla tree types
		for(BlockPlanks.EnumType woodType: BlockPlanks.EnumType.values()) {
			Species species = TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, woodType.getName().replace("_","")));
			ItemStack saplingStack = new ItemStack(Blocks.SAPLING, 1, woodType.getMetadata());
			ItemStack seedStack = species.getSeedStack(1);
			createDirtBucketExchangeRecipes(saplingStack, seedStack, true);
		}
		
		//Create an apple seed from an apple and dirt bucket
		if(ModConfigs.enableAppleTrees) {
			createDirtBucketExchangeRecipes(new ItemStack(Items.APPLE), TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, "apple")).getSeedStack(1), false);
		}
		
	}
	
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling) {
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, "seed");
	}
	
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling, String suffix) {
		if(!saplingStack.isEmpty() && !seedStack.isEmpty() && seedStack.getItem() instanceof Seed) {
			
			Seed seed = (Seed) seedStack.getItem();
			String speciesPath = seed.getSpecies(seedStack).getRegistryName().getResourcePath();
			String speciesDomain = seed.getSpecies(seedStack).getRegistryName().getResourceDomain();
			
			//Create a seed from a sapling and dirt bucket
			GameRegistry.addShapelessRecipe(
					new ResourceLocation(speciesDomain, speciesPath + suffix),
					null,
					seedStack,
					new Ingredient[]{
							Ingredient.fromStacks(saplingStack),
							Ingredient.fromItem(ModItems.dirtBucket)
					}
					);
			
			if(seedIsSapling) {
				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(
						new ResourceLocation(speciesDomain, speciesPath + "sapling"),
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
	}
}
