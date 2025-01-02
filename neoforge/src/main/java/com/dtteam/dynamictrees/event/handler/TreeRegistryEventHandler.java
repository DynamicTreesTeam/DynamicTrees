package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.event.TypeRegistryEvent;
import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpreadableSoilProperties;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
import com.dtteam.dynamictrees.systems.cell.CellKits;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatures;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKits;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.MangroveFamily;
import com.dtteam.dynamictrees.tree.family.NetherFungusFamily;
import com.dtteam.dynamictrees.tree.family.PalmFamily;
import com.dtteam.dynamictrees.tree.species.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class TreeRegistryEventHandler {

    @SubscribeEvent
    public static void registerSpecies(final RegistryEvent<Species> event) {
//        // Registers fake species for generating mushrooms.
//        event.getRegistry().registerAll(new FakeMushroomSpecies(true), new FakeMushroomSpecies(false));
    }

    @SubscribeEvent
    public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
        event.registerType(DynamicTrees.location("solid"), SolidLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("wart"), WartProperties.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("scruffy"), ScruffyLeavesProperties.TYPE);
    }

    @SubscribeEvent
    public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusFamily.TYPE);
        event.registerType(DynamicTrees.location("mangrove"), MangroveFamily.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusSpecies.TYPE);
        event.registerType(DynamicTrees.location("swamp_oak"), SwampOakSpecies.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmSpecies.TYPE);
        event.registerType(DynamicTrees.location("mangrove"), MangroveSpecies.TYPE);
    }

    @SubscribeEvent
    public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
        event.registerType(DynamicTrees.location("water"), WaterSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("spreadable"), SpreadableSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
    }

    ///////////////////////////////////////////
    // CUSTOM TREE LOGIC
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
        CellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
        GrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
        GenFeatures.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        final List<SimpleRegistry<?>> registries = Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .map(registry -> (SimpleRegistry<?>) registry)
                .collect(Collectors.toList());

        // Post registry events.
        registries.forEach(SimpleRegistry::postRegistryEvent);

//        Resources.setupTreesResourceManager();
//
//        // Register Forge registry entry getters and add-on Json object getters.
//        JsonDeserializers.registerForgeEntryGetters();
        JsonDeserializers.postRegistryEvent();
//
//        // Register feature cancellers.
//        FeatureCanceller.REGISTRY.postRegistryEvent();
//        FeatureCanceller.REGISTRY.lock();
    }

    @SubscribeEvent
    public static void loadResources(RegisterEvent event) {
        if (event.getRegistryKey() != BuiltInRegistries.BLOCK.key()) {
            return;
        }
//        // Register any registry entries from Json files.
//        Resources.MANAGER.load();
//        // Lock all the registries.
//        Registries.REGISTRIES.stream()
//                .filter(registry -> registry instanceof SimpleRegistry)
//                .forEach(Registry::lock);
    }
}
