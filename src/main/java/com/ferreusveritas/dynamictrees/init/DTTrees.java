package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.WartProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.*;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.TypeRegistryEvent;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static ResourceLocation OAK = DynamicTrees.resLoc("oak");
	public static ResourceLocation BIRCH = DynamicTrees.resLoc("birch");
	public static ResourceLocation SPRUCE = DynamicTrees.resLoc("spruce");
	public static ResourceLocation JUNGLE = DynamicTrees.resLoc("jungle");
	public static ResourceLocation DARK_OAK = DynamicTrees.resLoc("dark_oak");
	public static ResourceLocation ACACIA = DynamicTrees.resLoc("acacia");
	public static ResourceLocation CRIMSON = DynamicTrees.resLoc("crimson");
	public static ResourceLocation WARPED = DynamicTrees.resLoc("warped");

	@SubscribeEvent
	public static void registerSpecies (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<Species> event) {
		// Registers fake species for generating mushrooms.
		event.getRegistry().registerAll(new Mushroom(true), new Mushroom(false));
	}

	@SubscribeEvent
	public static void registerLeavesPropertiesTypes (final TypeRegistryEvent<LeavesProperties> event) {
		event.registerType(DynamicTrees.resLoc("wart"), new WartProperties.Type());
	}

	@SubscribeEvent
	public static void registerFamilyTypes (final TypeRegistryEvent<Family> event) {
		event.registerType(DynamicTrees.resLoc("fungus"), new FungusFamily.Type());
	}

	@SubscribeEvent
	public static void registerSpeciesTypes (final TypeRegistryEvent<Species> event) {
		event.registerType(DynamicTrees.resLoc("fungus"), new FungusSpecies.Type());
		event.registerType(DynamicTrees.resLoc("dark_oak"), new DarkOakSpecies.Type());
	}

	public static final ResourceLocation NULL = resLoc("null");

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		final List<Registry<?>> registries = Arrays.asList(CellKit.REGISTRY, LeavesProperties.REGISTRY, GrowthLogicKit.REGISTRY, GenFeature.REGISTRY, Family.REGISTRY, Species.REGISTRY);

		// Register all registry entry types, and then all registries.
		registries.forEach(registry -> {
			if (registry instanceof TypedRegistry)
				((TypedRegistry<?, ?>) registry).postTypeRegistryEvent();

			registry.postRegistryEvent();
		});

		DTResourceRegistries.setupTreesResourceManager();

		// Register Forge registry entry getters and add-on Json object getters.
		JsonObjectGetters.registerForgeEntryGetters();
		JsonObjectGetters.postRegistryEvent();

		// Register any registry entries from Json files.
		DTResourceRegistries.TREES_RESOURCE_MANAGER.load();

		// Lock all the registries.
		registries.forEach(Registry::lock);

		// Register feature cancellers.
		FeatureCanceller.REGISTRY.postRegistryEvent();
		FeatureCanceller.REGISTRY.lock();
	}

	private static ResourceLocation resLoc (final String path) {
		return DynamicTrees.resLoc(path);
	}

}
