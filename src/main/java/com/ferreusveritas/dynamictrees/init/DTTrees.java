package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
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

	/**
	 * Pay Attn! This should be run after the Dynamic Trees Mod
	 * has created it's Blocks and Items.  These trees depend
	 * on the Dynamic Sapling
	 */
	public static void setupTrees() {
		Species.REGISTRY.register(Species.NULL_SPECIES.setRegistryName(new ResourceLocation(DynamicTrees.MOD_ID, "null")));
		Collections.addAll(baseFamilies, new OakTree(), new SpruceTree(), new BirchTree(), new JungleTree(), new AcaciaTree(), new DarkOakTree(), new CrimsonFungus(), new WarpedFungus());
		baseFamilies.forEach(tree -> tree.registerSpecies(Species.REGISTRY));

		// Registers a fake species for generating mushrooms
		Species.REGISTRY.register(new Mushroom(true));
		Species.REGISTRY.register(new Mushroom(false));
	}
	
	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		Species.REGISTRY = new RegistryBuilder<Species>()
				.setName(new ResourceLocation(DynamicTrees.MOD_ID, "species"))
				.setDefaultKey(new ResourceLocation(DynamicTrees.MOD_ID, "null"))
				.disableSaving()
				.setType(Species.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();

		GenFeature.REGISTRY = new RegistryBuilder<GenFeature>()
				.setName(new ResourceLocation(DynamicTrees.MOD_ID, "gen_feature"))
				.setDefaultKey(new ResourceLocation(DynamicTrees.MOD_ID, "null"))
				.disableSaving()
				.setType(GenFeature.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();
		
		setupTrees();
	}
	
}
