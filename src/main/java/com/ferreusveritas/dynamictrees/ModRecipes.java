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
			Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(ItemBlock.getItemFromBlock(Blocks.DIRT)));
		
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
		ResourceLocation name = (seedStack.getItem() instanceof Seed)? ((Seed) seedStack.getItem()).getSpecies(seedStack).getRegistryName() : seedStack.getItem().getRegistryName();
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, suffix, name);
	}
	public static void createDirtBucketExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, boolean seedIsSapling, String suffix, ResourceLocation species) {
		if(!saplingStack.isEmpty() && !seedStack.isEmpty()) {
			String speciesPath = species.getResourcePath();
			String speciesDomain = species.getResourceDomain();
			
			//Create a seed from a sapling and dirt bucket
			GameRegistry.addShapelessRecipe(
					new ResourceLocation(speciesDomain, speciesPath + suffix),
					null,
					seedStack,
				Ingredient.fromStacks(saplingStack),
				Ingredient.fromItem(ModItems.dirtBucket));
			
			if(seedIsSapling) {
				//Creates a vanilla sapling from a seed and dirt bucket
				GameRegistry.addShapelessRecipe(
						new ResourceLocation(speciesDomain, speciesPath + "sapling"),
						null,
						saplingStack,
					Ingredient.fromStacks(seedStack),
					Ingredient.fromItem(ModItems.dirtBucket));
				
				//Register the seed in the ore dictionary as a sapling since we can convert for free anyway.
				OreDictionary.registerOre("treeSapling", seedStack);
			}
		}
	}

	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, boolean requiresBonemeal) {
		createDirtBucketExchangeRecipesWithFruit(saplingStack, seedStack, fruitStack, seedIsSapling, "seed", requiresBonemeal);
	}
	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, String suffix, boolean requiresBonemeal) {
		ResourceLocation name = (seedStack.getItem() instanceof Seed)? ((Seed) seedStack.getItem()).getSpecies(seedStack).getRegistryName() : seedStack.getItem().getRegistryName();
		createDirtBucketExchangeRecipesWithFruit(saplingStack, seedStack, fruitStack, seedIsSapling, suffix, name, requiresBonemeal);
	}
	public static void createDirtBucketExchangeRecipesWithFruit(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, String suffix, ResourceLocation species, boolean requiresBonemeal) {
		//Creates all the recipes not involving the fruit
		createDirtBucketExchangeRecipes(saplingStack, seedStack, seedIsSapling, suffix, species);
		createFruitOnlyExchangeRecipes(saplingStack, seedStack, fruitStack, seedIsSapling, species, requiresBonemeal);
	}

	public static void createFruitOnlyExchangeRecipes(ItemStack saplingStack, ItemStack seedStack, ItemStack fruitStack, boolean seedIsSapling, ResourceLocation species, boolean requiresBonemeal) {
		if (fruitStack != null && !fruitStack.isEmpty()){
			if (!requiresBonemeal){
				//Creates a seed from fruit
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(species.getResourceDomain(), species.getResourcePath() + "seedfromfruit"),
					null,
					seedStack,
					Ingredient.fromStacks(fruitStack));
				//Creates a sapling from fruit and a dirt bucket
				if (seedIsSapling){
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(species.getResourceDomain(), species.getResourcePath()+"saplingfromfruit"),
					null,
					saplingStack,
					Ingredient.fromStacks(fruitStack),
					Ingredient.fromItem(com.ferreusveritas.dynamictrees.ModItems.dirtBucket));
				}
			} else {
				ItemStack bonemeal = new ItemStack(Items.DYE, 1, 15);
				//Creates a seed from fruit using bonemeal
				GameRegistry.addShapelessRecipe(
					new ResourceLocation(species.getResourceDomain(), species.getResourcePath() + "seedfromfruitgerminate"),
					null,
					seedStack,
					Ingredient.fromStacks(fruitStack),
					Ingredient.fromStacks(bonemeal));
				//Creates a sapling from fruit and a dirt bucket using bonemeal
				if (seedIsSapling) {
					GameRegistry.addShapelessRecipe(
						new ResourceLocation(species.getResourceDomain(), species.getResourcePath() + "saplingfromfruit"),
						null,
						saplingStack,
						Ingredient.fromStacks(fruitStack),
						Ingredient.fromStacks(bonemeal),
						Ingredient.fromItem(com.ferreusveritas.dynamictrees.ModItems.dirtBucket));
				}
			}
		}
	}
	
}
