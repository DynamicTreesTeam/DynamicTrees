package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
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
	public static final String DARK_OAK = "dark_oak";
	public static final String ACACIA = "acacia";
	
	public static final String CONIFER = "conifer";
	
	public static ArrayList<VanillaTreeFamily> baseFamilies = new ArrayList<>();
	// keeping the cactus 'tree' out of baseTrees prevents automatic registration of seed/sapling conversion recipes, transformation potion recipes, and models
	public static Cactus dynamicCactus;
	
	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void setupTrees() {
		Species.REGISTRY.register(Species.NULLSPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MODID, "null")));
		Collections.addAll(baseFamilies, new OakTree(), new SpruceTree(), new BirchTree(), new JungleTree(), new AcaciaTree(), new DarkOakTree(), new CrimsonFungus(), new WarpedFungus());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));
		dynamicCactus = new Cactus();
		dynamicCactus.registerSpecies(Species.REGISTRY);
		
		//Registers a fake species for generating mushrooms
		Species.REGISTRY.register(new Mushroom(true));
		Species.REGISTRY.register(new Mushroom(false));

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
