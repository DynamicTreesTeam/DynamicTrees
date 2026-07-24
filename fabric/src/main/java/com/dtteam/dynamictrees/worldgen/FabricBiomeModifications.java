package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellationRegistry;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FabricBiomeModifications {

    private static final Identifier REMOVE_TREES_ID = DynamicTrees.location("remove_vanilla_trees");
    private static final Identifier ADD_TREES_ID = DynamicTrees.location("add_dynamic_trees");
    public static final TagKey<PlacedFeature> FEATURE_CANCELLER_EXCLUSIONS_KEY = TagKey.create(
            net.minecraft.core.registries.Registries.PLACED_FEATURE,
            DynamicTrees.location("feature_canceller_exclusions"));

    public static void register() {
        BiomeModifications.create(REMOVE_TREES_ID)
                .add(ModificationPhase.REMOVALS, BiomeSelectors.all(), (selectionContext, modificationContext) -> {
                    if(DTConfigs.SERVER_CONFIG.isLoaded()) {
                        if (!DTConfigs.SERVER.worldGen.get()) {
                            return;
                        }
                    }
                    removeVanillaTrees(selectionContext, modificationContext);
                });

        BiomeModifications.create(ADD_TREES_ID)
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.all(), (selectionContext, modificationContext) -> {
                    if(DTConfigs.SERVER_CONFIG.isLoaded()) {
                        if (!DTConfigs.SERVER.worldGen.get()) {
                            return;
                        }
                    }
                    addDynamicTrees(modificationContext);
                });
    }

    private static void removeVanillaTrees(BiomeSelectionContext selectionContext, BiomeModificationContext context) {
        ResourceKey<Biome> biomeKey = selectionContext.getBiomeKey();

        BiomePropertySelectors.NormalFeatureCancellation featureCancellations = new BiomePropertySelectors.NormalFeatureCancellation();

        for (FeatureCancellationRegistry.Entry entry : FeatureCancellationRegistry.getCancellations()) {
            if (entry.biomes().containsKey(biomeKey)) {
                if (entry.operation() == BiomeDatabase.Operation.REPLACE) {
                    featureCancellations.reset();
                }
                featureCancellations.addFrom(entry.cancellations());
            }
        }

        List<ResourceKey<PlacedFeature>> featuresToRemove = new ArrayList<>();

        for (GenerationStep.Decoration stage : featureCancellations.getDecorationSteps()) {
            int stageIndex = stage.ordinal();
            List<HolderSet<PlacedFeature>> features = selectionContext.getBiomeHolder()
                    .value()
                    .getGenerationSettings()
                    .features();

            if (stageIndex >= features.size()) {
                continue;
            }

            HolderSet<PlacedFeature> stageFeatures = features.get(stageIndex);

            for (Holder<PlacedFeature> placedFeatureHolder : stageFeatures) {
                if (placedFeatureHolder.is(FEATURE_CANCELLER_EXCLUSIONS_KEY)) {
                    continue;
                }

                PlacedFeature placedFeature = placedFeatureHolder.value();

                boolean shouldCancel = placedFeature.getFeatures().anyMatch(configuredFeatureHolder -> {
                    for (FeatureCanceller featureCanceller : featureCancellations.getCancellers()) {
                        if (featureCanceller.shouldCancel(configuredFeatureHolder.value(), featureCancellations)) {
                            return true;
                        }
                    }
                    return false;
                });

                if (shouldCancel) {
                    Optional<ResourceKey<PlacedFeature>> keyOpt = selectionContext.getPlacedFeatureKey(placedFeature);
                    keyOpt.ifPresent(featuresToRemove::add);
                }
            }
        }

        for (ResourceKey<PlacedFeature> key : featuresToRemove) {
            context.getGenerationSettings().removeFeature(key);
        }
    }

    private static void addDynamicTrees(BiomeModificationContext context) {
        context.getGenerationSettings().addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION,
                DTRegistries.CAVE_ROOTED_TREE_PLACED_FEATURE
        );
        context.getGenerationSettings().addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION,
                DTRegistries.DYNAMIC_TREE_PLACED_FEATURE
        );
    }
}
