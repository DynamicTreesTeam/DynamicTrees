package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.registry.Registries;
import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpreadableSoilProperties;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.platform.FabricMiscHelper;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.cell.CellKits;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatures;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKits;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.NetherFungusFamily;
import com.dtteam.dynamictrees.tree.family.PalmFamily;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.dtteam.dynamictrees.tree.species.*;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

import java.util.List;
import java.util.stream.Collectors;

public class ModEventHandler {

    public static void RegisterEvents(){

//        @SubscribeEvent
//        public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
//            if (!event.isEntryOfType(LeavesProperties.class)) return;
//            event.registerType(DynamicTrees.location("solid"), SolidLeavesProperties.TYPE);
//            event.registerType(DynamicTrees.location("wart"), WartProperties.TYPE);
//            event.registerType(DynamicTrees.location("palm"), PalmLeavesProperties.TYPE);
//            event.registerType(DynamicTrees.location("scruffy"), ScruffyLeavesProperties.TYPE);
//        }
//
//        @SubscribeEvent
//        public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
//            if (!event.isEntryOfType(Family.class)) return;
//            event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusFamily.TYPE);
//            event.registerType(DynamicTrees.location("underground_roots"), UndergroundRootsFamily.TYPE);
//            event.registerType(DynamicTrees.location("palm"), PalmFamily.TYPE);
//        }
//
//        @SubscribeEvent
//        public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
//            if (!event.isEntryOfType(Species.class)) return;
//            event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusSpecies.TYPE);
//            event.registerType(DynamicTrees.location("swamp"), SwampSpecies.TYPE);
//            event.registerType(DynamicTrees.location("palm"), PalmSpecies.TYPE);
//            event.registerType(DynamicTrees.location("underground_roots"), UndergroundRootsSpecies.TYPE);
//        }
//
//        @SubscribeEvent
//        public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
//            if (!event.isEntryOfType(SoilProperties.class)) return;
//            event.registerType(DynamicTrees.location("water"), WaterSoilProperties.TYPE);
//            event.registerType(DynamicTrees.location("spreadable"), SpreadableSoilProperties.TYPE);
//            event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
//        }
//
//        ///////////////////////////////////////////
//        // CUSTOM TREE LOGIC
//        ///////////////////////////////////////////
//
//        @SubscribeEvent
//        public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
//            if (!event.isEntryOfType(CellKit.class)) return;
//            CellKits.register(event.getRegistry());
//        }
//
//        @SubscribeEvent
//        public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
//            if (!event.isEntryOfType(GrowthLogicKit.class)) return;
//            GrowthLogicKits.register(event.getRegistry());
//        }
//
//        @SubscribeEvent
//        public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
//            if (!event.isEntryOfType(GenFeature.class)) return;
//            GenFeatures.register(event.getRegistry());
//        }
//
//        @SubscribeEvent
//        public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
//            if (!event.isEntryOfType(FeatureCanceller.class)) return;
//            FeatureCancellers.register(event.getRegistry());
//        }

        DynamicRegistrySetupCallback.EVENT.register((dynamicRegistryView -> {
            final List<SimpleRegistry<?>> registries = Registries.REGISTRIES.stream()
                    .filter(registry -> registry instanceof SimpleRegistry)
                    .map(registry -> (SimpleRegistry<?>) registry)
                    .collect(Collectors.toList());

            // Post registry events.
            registries.forEach(SimpleRegistry::postRegistryEvent);

            Resources.setupTreesResourceManager();

            // Register Forge registry entry getters and add-on Json object getters.
            JsonDeserializers.registerRegistryEntryGetters();
            JsonDeserializers.postRegistryEvent();

            // Register feature cancellers.
            FeatureCanceller.REGISTRY.postRegistryEvent();
            FeatureCanceller.REGISTRY.lock();
        }));

        //Register any registry entries from Json files.
        Resources.MANAGER.load();
        // Lock all the registries.
        Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .forEach(Registry::lock);

    }

}
