package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.registry.Registries;
import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.block.leaves.ScruffyLeavesProperties;
import com.dtteam.dynamictrees.block.leaves.SolidLeavesProperties;
import com.dtteam.dynamictrees.block.leaves.WartProperties;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpreadableSoilProperties;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.systems.cell.CellKits;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatures;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKits;
import com.dtteam.dynamictrees.tree.family.*;
import com.dtteam.dynamictrees.tree.species.NetherFungusSpecies;
import com.dtteam.dynamictrees.tree.species.PalmSpecies;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.tree.species.SwampSpecies;
import com.dtteam.dynamictrees.tree.species.AerialRootsSpecies;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellers;

import java.util.List;
import java.util.stream.Collectors;

public class ModEventHandler {

    public static void RegisterEvents(){
        registerLeavesPropertiesTypes();
        registerFamilyTypes();
        registerSpeciesTypes();
        registerSoilPropertiesTypes();
        registerCellKits();
        registerGrowthLogicKits();
        registerGenFeatures();
        registerFeatureCancellers();

        final List<SimpleRegistry<?>> registries = Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .map(registry -> (SimpleRegistry<?>) registry)
                .collect(Collectors.toList());

        registries.forEach(SimpleRegistry::postRegistryEvent);

        Resources.setupTreesResourceManager();

        JsonDeserializers.registerRegistryEntryGetters();
        JsonDeserializers.postRegistryEvent();

        FeatureCanceller.REGISTRY.postRegistryEvent();
        FeatureCanceller.REGISTRY.lock();

        Resources.MANAGER.load();
        Registries.REGISTRIES.stream()
                .filter(registry -> registry instanceof SimpleRegistry)
                .forEach(Registry::lock);
    }

    private static void registerLeavesPropertiesTypes() {
        LeavesProperties.REGISTRY.registerType(DynamicTrees.location("solid"), SolidLeavesProperties.TYPE);
        LeavesProperties.REGISTRY.registerType(DynamicTrees.location("wart"), WartProperties.TYPE);
        LeavesProperties.REGISTRY.registerType(DynamicTrees.location("palm"), PalmLeavesProperties.TYPE);
        LeavesProperties.REGISTRY.registerType(DynamicTrees.location("scruffy"), ScruffyLeavesProperties.TYPE);
    }

    private static void registerFamilyTypes() {
        Family.REGISTRY.registerType(DynamicTrees.location("nether_fungus"), NetherFungusFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTrees.location("aerial_roots"), AerialRootsFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTrees.location("mossy_aerial_roots"), MossyAerialRootsFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTrees.location("palm"), PalmFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTrees.location("alt_branch"), AltBranchFamily.TYPE);
        Family.REGISTRY.registerType(DynamicTrees.location("creaking_heart"), CreakingHeartFamily.TYPE);
    }

    private static void registerSpeciesTypes() {
        Species.REGISTRY.registerType(DynamicTrees.location("nether_fungus"), NetherFungusSpecies.TYPE);
        Species.REGISTRY.registerType(DynamicTrees.location("swamp"), SwampSpecies.TYPE);
        Species.REGISTRY.registerType(DynamicTrees.location("palm"), PalmSpecies.TYPE);
        Species.REGISTRY.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSpecies.TYPE);
    }

    private static void registerSoilPropertiesTypes() {
        SoilProperties.REGISTRY.registerType(DynamicTrees.location("water"), WaterSoilProperties.TYPE);
        SoilProperties.REGISTRY.registerType(DynamicTrees.location("spreadable"), SpreadableSoilProperties.TYPE);
        SoilProperties.REGISTRY.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
    }

    private static void registerCellKits() {
        CellKits.register(CellKit.REGISTRY);
    }

    private static void registerGrowthLogicKits() {
        GrowthLogicKits.register(GrowthLogicKit.REGISTRY);
    }

    private static void registerGenFeatures() {
        GenFeatures.register(GenFeature.REGISTRY);
    }

    private static void registerFeatureCancellers() {
        FeatureCancellers.register(FeatureCanceller.REGISTRY);
    }

}
