package com.dtteam.dynamictrees.init;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DTTrees {

    @SubscribeEvent
    public static void registerSpecies(final RegistryEvent<Species> event) {
//        // Registers fake species for generating mushrooms.
//        event.getRegistry().registerAll(new FakeMushroomSpecies(true), new FakeMushroomSpecies(false));
    }

//    @SubscribeEvent
//    public static void registerSoilProperties(final RegistryEvent<SoilProperties> event) {
//
//    }

    @SubscribeEvent
    public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
//        event.registerType(DynamicTreesCommon.location("solid"), SolidLeavesProperties.TYPE);
//        event.registerType(DynamicTreesCommon.location("wart"), WartProperties.TYPE);
//        event.registerType(DynamicTreesCommon.location("palm"), PalmLeavesProperties.TYPE);
//        event.registerType(DynamicTreesCommon.location("scruffy"), ScruffyLeavesProperties.TYPE);
    }

    @SubscribeEvent
    public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
//        event.registerType(DynamicTreesCommon.location("nether_fungus"), NetherFungusFamily.TYPE);
//        event.registerType(DynamicTreesCommon.location("mangrove"), MangroveFamily.TYPE);
//        event.registerType(DynamicTreesCommon.location("palm"), PalmFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
//        event.registerType(DynamicTreesCommon.location("nether_fungus"), NetherFungusSpecies.TYPE);
//        event.registerType(DynamicTreesCommon.location("swamp_oak"), SwampOakSpecies.TYPE);
//        event.registerType(DynamicTreesCommon.location("palm"), PalmSpecies.TYPE);
//        event.registerType(DynamicTreesCommon.location("mangrove"), MangroveSpecies.TYPE);
    }

//    @SubscribeEvent
//    public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
//        event.registerType(DynamicTreesCommon.location("water"), WaterSoilProperties.TYPE);
//        event.registerType(DynamicTreesCommon.location("spreadable"), SpreadableSoilProperties.TYPE);
//        event.registerType(DynamicTreesCommon.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
//    }

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
//        JsonDeserialisers.registerForgeEntryGetters();
//        JsonDeserialisers.postRegistryEvent();
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
