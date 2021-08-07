package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.registry.*;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.PalmLeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.SolidLeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.WartProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.*;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Mushroom;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.families.NetherFungusFamily;
import com.ferreusveritas.dynamictrees.trees.species.NetherFungusSpecies;
import com.ferreusveritas.dynamictrees.trees.species.PalmSpecies;
import com.ferreusveritas.dynamictrees.trees.species.SwampOakSpecies;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedList;
import net.minecraft.world.gen.blockstateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.feature.Features;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static final ResourceLocation NULL = DynamicTrees.resLoc("null");

	public static final ResourceLocation OAK = DynamicTrees.resLoc("oak");
	public static final ResourceLocation BIRCH = DynamicTrees.resLoc("birch");
	public static final ResourceLocation SPRUCE = DynamicTrees.resLoc("spruce");
	public static final ResourceLocation JUNGLE = DynamicTrees.resLoc("jungle");
	public static final ResourceLocation DARK_OAK = DynamicTrees.resLoc("dark_oak");
	public static final ResourceLocation ACACIA = DynamicTrees.resLoc("acacia");
	public static final ResourceLocation CRIMSON = DynamicTrees.resLoc("crimson");
	public static final ResourceLocation WARPED = DynamicTrees.resLoc("warped");

	@SubscribeEvent
	public static void registerSpecies (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<Species> event) {
		// Registers fake species for generating mushrooms.
		event.getRegistry().registerAll(new Mushroom(true), new Mushroom(false));
	}

	@SubscribeEvent
	public static void registerSoilProperties (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<SoilProperties> event) {
		event.getRegistry().registerAll(
				//SoilHelper.registerSoil(DynamicTrees.resLoc("dirt"),Blocks.DIRT, SoilHelper.DIRT_LIKE, ),//new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM)
				//SoilHelper.registerSoil(DynamicTrees.resLoc("netherrack"),Blocks.NETHERRACK, SoilHelper.NETHER_LIKE, new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM))
		);
	}

	@SubscribeEvent
	public static void registerLeavesPropertiesTypes (final TypeRegistryEvent<LeavesProperties> event) {
		event.registerType(DynamicTrees.resLoc("solid"), SolidLeavesProperties.TYPE);
		event.registerType(DynamicTrees.resLoc("wart"), WartProperties.TYPE);
		event.registerType(DynamicTrees.resLoc("palm"), PalmLeavesProperties.TYPE);
	}

	@SubscribeEvent
	public static void registerFamilyTypes (final TypeRegistryEvent<Family> event) {
		event.registerType(DynamicTrees.resLoc("nether_fungus"), NetherFungusFamily.TYPE);
	}

	@SubscribeEvent
	public static void registerSpeciesTypes (final TypeRegistryEvent<Species> event) {
		event.registerType(DynamicTrees.resLoc("nether_fungus"), NetherFungusSpecies.TYPE);
		event.registerType(DynamicTrees.resLoc("swamp_oak"), SwampOakSpecies.TYPE);
		event.registerType(DynamicTrees.resLoc("palm"), PalmSpecies.TYPE);
	}

	@SubscribeEvent
	public static void registerSoilPropertiesTypes (final TypeRegistryEvent<SoilProperties> event) {
		event.registerType(DynamicTrees.resLoc("water"), WaterSoilProperties.TYPE);
		event.registerType(DynamicTrees.resLoc("spreadable"), SpreadableSoilProperties.TYPE);
	}

	@SubscribeEvent
	public static void newRegistry(RegistryEvent.NewRegistry event) {
		final List<Registry<?>> registries = Registries.REGISTRIES.stream()
				.filter(registry -> registry instanceof Registry)
				.map(registry -> (Registry<?>) registry)
				.collect(Collectors.toList());

		// Post registry events.
		registries.forEach(Registry::postRegistryEvent);

		DTResourceRegistries.setupTreesResourceManager();

		// Register Forge registry entry getters and add-on Json object getters.
		JsonDeserialisers.registerForgeEntryGetters();
		JsonDeserialisers.postRegistryEvent();

		// Register any registry entries from Json files.
		DTResourceRegistries.TREES_RESOURCE_MANAGER.load();

		// Lock all the registries.
		registries.forEach(Registry::lock);

		// Register feature cancellers.
		FeatureCanceller.REGISTRY.postRegistryEvent();
		FeatureCanceller.REGISTRY.lock();
	}

	public static void replaceNyliumFungiFeatures() {
		TreeRegistry.findSpecies(CRIMSON).getSapling().ifPresent(crimsonSapling ->
				TreeRegistry.findSpecies(WARPED).getSapling().ifPresent(warpedSapling -> {
					replaceFeatureConfigs(((WeightedBlockStateProvider) Features.Configs.CRIMSON_FOREST_CONFIG.stateProvider), crimsonSapling, warpedSapling);
					replaceFeatureConfigs(((WeightedBlockStateProvider) Features.Configs.WARPED_FOREST_CONFIG.stateProvider), crimsonSapling, warpedSapling);
				})
		);
	}

	private static void replaceFeatureConfigs(WeightedBlockStateProvider featureConfig, Block crimsonSapling, Block warpedSapling) {
		for (final WeightedList.Entry<BlockState> entry : featureConfig.weightedList.entries) {
			if (entry.data.getBlock() == Blocks.CRIMSON_FUNGUS)
				entry.data = crimsonSapling.defaultBlockState();
			if (entry.data.getBlock() == Blocks.WARPED_FUNGUS)
				entry.data = warpedSapling.defaultBlockState();
		}
	}

}
