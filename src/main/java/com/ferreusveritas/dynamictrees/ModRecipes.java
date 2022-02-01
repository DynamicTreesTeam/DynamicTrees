package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ModConstants.MODID)
public class ModRecipes {
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void register(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> registry = event.getRegistry();
		
		ModItems.dendroPotion.registerRecipes(registry);
		
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ResourceLocation(ModConstants.MODID, "dirtbucket"), null, new ItemStack(ModItems.dirtBucket),
			Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(Item.getItemFromBlock(Blocks.DIRT)));
		
		//Create a seed <-> sapling exchange for the 6 vanilla tree types
		for (BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values()) {
			Species species = TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, woodType.getName().replace("_", "")));
			ItemStack saplingStack = new ItemStack(Blocks.SAPLING, 1, woodType.getMetadata());
			ItemStack seedStack = species.getSeedStack(1);
			createDirtBucketExchangeRecipes(saplingStack, seedStack, true);
		}
		
		//Create an apple seed from an apple and dirt bucket
		if (ModConfigs.enableAppleTrees) {
			createDirtBucketExchangeRecipes(new ItemStack(Items.APPLE), TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, "apple")).getSeedStack(1), false);
		}
		
	}
	
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling) {
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, "seed");
	}
	
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling, String suffix) {
		ResourceLocation name = (seedStack.getItem() instanceof Seed) ? ((Seed) seedStack.getItem()).getSpecies(seedStack).getRegistryName() : seedStack.getItem().getRegistryName();
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, suffix, name);
	}
	
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling, String suffix, ResourceLocation species) {
		createSaplingToSeedWithDirtBucketRecipe(saplingStack, seedStack, suffix, species);
		if(seedIsSapling) {
			createSeedToSaplingWithDirtBucketRecipe(seedStack, saplingStack, "sapling", species);
		}
	}
	
	public static void createSeedToSaplingWithDirtBucketRecipe(ItemStack seedStack, ItemStack saplingStack, String suffix, ResourceLocation species) {
		if(ModConfigs.compatRecipeForSaplings) {
			if (!saplingStack.isEmpty() && !seedStack.isEmpty()) {
				String speciesPath = species.getResourcePath();
				String speciesDomain = species.getResourceDomain();
				
				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(speciesDomain, speciesPath + suffix),
					null,
					saplingStack,
					Ingredient.fromStacks(seedStack),
					Ingredient.fromItem(ModItems.dirtBucket));
				
				//Register the seed in the ore dictionary as a sapling since we can convert for free anyway.
				OreDictionary.registerOre("treeSapling", seedStack);
			}
		}
	}
	
	public static void createSaplingToSeedWithDirtBucketRecipe(ItemStack saplingStack, ItemStack seedStack, String suffix, ResourceLocation species) {
		if (!saplingStack.isEmpty() && !seedStack.isEmpty()) {
			String speciesPath = species.getResourcePath();
			String speciesDomain = species.getResourceDomain();
			
			//Create a seed from a sapling and dirt bucket
			GameRegistry.addShapelessRecipe(
				new ResourceLocation(speciesDomain, speciesPath + suffix),
				null,
				seedStack,
				Ingredient.fromStacks(saplingStack),
				Ingredient.fromItem(ModItems.dirtBucket));
		}
	}
	
	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, boolean requiresBonemeal) {
		createDirtBucketExchangeRecipesWithFruit(saplingStack, seedStack, fruitStack, seedIsSapling, "seed", requiresBonemeal);
	}
	
	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, String suffix, boolean requiresBonemeal) {
		ResourceLocation name = (seedStack.getItem() instanceof Seed) ? ((Seed) seedStack.getItem()).getSpecies(seedStack).getRegistryName() : seedStack.getItem().getRegistryName();
		createDirtBucketExchangeRecipesWithFruit(saplingStack, seedStack, fruitStack, seedIsSapling, suffix, name, requiresBonemeal);
	}
	
	/**
	 * Convenience function to do all of the work of creating the recipes for a fruit tree.
	 * 
	 * @param saplingStack
	 * @param seedStack
	 * @param fruitStack
	 * @param seedIsSapling
	 * @param suffix
	 * @param species
	 * @param requiresBonemeal
	 */
	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, String suffix, ResourceLocation species, boolean requiresBonemeal) {
		//Creates all the recipes not involving the fruit
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, suffix, species);
		createFruitToSeedRecipe(seedStack, fruitStack, species, requiresBonemeal);
	}
	
	/** 
	 * Create a recipe that handles fruit to seed conversion.
	 *
	 * In order to maintain compatability and consistent behavior across all add-ons and the base DT mod a fruit should never be convertible to
	 * a seed without the use of a dirt bucket!!
	 * 
	 * @param seedStack The itemStack containing the seed item used to grow the tree
	 * @param fruitStack The itemStack for the fruit item such as a fig or cherries.
	 * @param species The Species of the fruit bearing tree
	 * @param requiresBonemeal True if the recipe also requires bonemeal
	 */
	private static void createFruitToSeedRecipe(ItemStack seedStack, ItemStack fruitStack, ResourceLocation species, boolean requiresBonemeal) {
		if (fruitStack != null && !fruitStack.isEmpty()) {
			ResourceLocation seedFromFruit = new ResourceLocation(species.getResourceDomain(), species.getResourcePath() + "seedfromfruit");
			Ingredient fruit = Ingredient.fromStacks(fruitStack);
			Ingredient bonemeal = Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 15));
			Ingredient dirtBucket = Ingredient.fromItem(com.ferreusveritas.dynamictrees.ModItems.dirtBucket);//Dirt Bucket is not optional!
			if (requiresBonemeal) {
				//Convert fruit to seed using bonemeal and a dirt bucket
				GameRegistry.addShapelessRecipe(seedFromFruit, null, seedStack, fruit, bonemeal, dirtBucket);
			} else {
				//Convert fruit to seed using a dirt bucket
				GameRegistry.addShapelessRecipe(seedFromFruit, null, seedStack, fruit, dirtBucket);
			}
		}
	}
	
}
