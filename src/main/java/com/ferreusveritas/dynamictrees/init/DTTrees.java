package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static final String NULL = "null";
	public static final String OAK = "oak";
	public static final String BIRCH = "birch";
	public static final String SPRUCE = "spruce";
	public static final String JUNGLE = "jungle";
	public static final String DARKOAK = "darkoak";
	public static final String ACACIA = "acacia";

	public static final String CONIFER = "conifer";

	public static ArrayList<TreeFamilyVanilla> baseFamilies = new ArrayList<>();
	// keeping the cactus 'tree' out of baseTrees prevents automatic registration of seed/sapling conversion recipes, transformation potion recipes, and models
    public static TreeCactus dynamicCactus;

	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void setupTrees() {
		Species.REGISTRY.register(Species.NULLSPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "null")));
		Collections.addAll(baseFamilies, new TreeOak(), new TreeSpruce(), new TreeBirch(), new TreeJungle(), new TreeAcacia(), new TreeDarkOak());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
        dynamicCactus = new TreeCactus();
        dynamicCactus.registerSpecies(Species.REGISTRY);

		//Registers a fake species for generating mushrooms
        Species.REGISTRY.register(new Mushroom(true));
        Species.REGISTRY.register(new Mushroom(false));

		RootyBlockHelper.excemptBlock(Blocks.FARMLAND, Blocks.DIRT); // We excempt farmland from having a custom rooty block and default to Dirt's rooty block.

		setupRootyBlocks(Species.REGISTRY.getValues()); // Rooty Dirt blocks are created for each allowed soil in the registry (except the previously excempt ones)


	}

	/**
	 * This method must be called by any addon that adds new allowed soils that arent vanilla
	 * @param speciesList list of species with new allowed soils to be rootified
	 */
	public static void setupRootyBlocks (Collection<Species> speciesList){
		for(Species species: speciesList) {
			for (Block soil : species.getAcceptableSoils()){
				if (!RootyBlockHelper.getRootyBlocksMap().containsKey(soil)){
					RootyBlockHelper.addToRootyBlocksMap(soil);
				}
			}
		}
	}

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		Species.REGISTRY = new RegistryBuilder<Species>()
				.setName(new ResourceLocation(DynamicTrees.MODID, "species"))
				.setDefaultKey(new ResourceLocation(DynamicTrees.MODID, "null"))
				.disableSaving()
				.setType(Species.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		setupTrees();
	}
	
}
