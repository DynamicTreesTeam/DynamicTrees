package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.soil.AerialRootsSoilProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpreadableSoilProperties;
import com.dtteam.dynamictrees.block.soil.WaterSoilProperties;
import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.event.TypeRegistryEvent;
import com.dtteam.dynamictrees.systems.cell.CellKits;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatures;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKits;
import com.dtteam.dynamictrees.tree.family.*;
import com.dtteam.dynamictrees.tree.species.*;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public class CommonModEventHandler {

    @SubscribeEvent
    public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
        if (!event.isEntryOfType(LeavesProperties.class)) return;
        event.registerType(DynamicTrees.location("solid"), SolidLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("wart"), WartProperties.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmLeavesProperties.TYPE);
        event.registerType(DynamicTrees.location("scruffy"), ScruffyLeavesProperties.TYPE);
    }

    @SubscribeEvent
    public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
        if (!event.isEntryOfType(Family.class)) return;
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusFamily.TYPE);
        event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsFamily.TYPE);
        event.registerType(DynamicTrees.location("mossy_aerial_roots"), MossyAerialRootsFamily.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmFamily.TYPE);
        event.registerType(DynamicTrees.location("alt_branch"), AltBranchFamily.TYPE);
        event.registerType(DynamicTrees.location("creaking_heart"), CreakingHeartFamily.TYPE);
    }

    @SubscribeEvent
    public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
        if (!event.isEntryOfType(Species.class)) return;
        event.registerType(DynamicTrees.location("nether_fungus"), NetherFungusSpecies.TYPE);
        event.registerType(DynamicTrees.location("swamp"), SwampSpecies.TYPE);
        event.registerType(DynamicTrees.location("palm"), PalmSpecies.TYPE);
        event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSpecies.TYPE);
    }

    @SubscribeEvent
    public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
        if (!event.isEntryOfType(SoilProperties.class)) return;
        event.registerType(DynamicTrees.location("water"), WaterSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("spreadable"), SpreadableSoilProperties.TYPE);
        event.registerType(DynamicTrees.location("aerial_roots"), AerialRootsSoilProperties.TYPE);
    }

    ///////////////////////////////////////////
    // CUSTOM TREE LOGIC
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
        if (!event.isEntryOfType(CellKit.class)) return;
        CellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
        if (!event.isEntryOfType(GrowthLogicKit.class)) return;
        GrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
        if (!event.isEntryOfType(GenFeature.class)) return;
        GenFeatures.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        if (!event.isEntryOfType(FeatureCanceller.class)) return;
        FeatureCancellers.register(event.getRegistry());
    }

}
