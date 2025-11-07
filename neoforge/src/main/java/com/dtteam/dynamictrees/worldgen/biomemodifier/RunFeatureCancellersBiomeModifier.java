package com.dtteam.dynamictrees.worldgen.biomemodifier;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellationRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public class RunFeatureCancellersBiomeModifier implements BiomeModifier {
    public static final TagKey<PlacedFeature> FEATURE_CANCELLER_EXCLUSIONS_KEY = TagKey.create(Registries.PLACED_FEATURE,
            DynamicTrees.location("feature_canceller_exclusions"));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.REMOVE && Services.CONFIG.getBoolConfig(IConfigHelper.WORLD_GEN)) {
            ResourceKey<Biome> biomeKey = biome.unwrapKey().orElseThrow();
            BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();

            BiomePropertySelectors.NormalFeatureCancellation featureCancellations = new BiomePropertySelectors.NormalFeatureCancellation();

            for (FeatureCancellationRegistry.Entry entry : FeatureCancellationRegistry.getCancellations()) {
                if (entry.biomes().containsKey(biomeKey)) {
                    if (entry.operation() == BiomeDatabase.Operation.REPLACE)
                        featureCancellations.reset();
                    featureCancellations.addFrom(entry.cancellations());
                }
            }

            featureCancellations.getDecorationSteps().forEach(stage -> generationSettings.getFeatures(stage).removeIf(placedFeatureHolder -> {
                // If you want a placed feature to be entirely excluded from cancellation by any feature cancellers,
                // add it to the dynamictrees:tags/worldgen/placed_feature/feature_canceller_exclusions tag.
                if (placedFeatureHolder.is(FEATURE_CANCELLER_EXCLUSIONS_KEY))
                    return false;

                PlacedFeature placedFeature = placedFeatureHolder.value();

                return placedFeature.getFeatures().anyMatch(configuredFeature -> {
                    for (FeatureCanceller featureCanceller : featureCancellations.getCancellers()) {
                        if (featureCanceller.shouldCancel(configuredFeature, featureCancellations)) {
                            return true;
                        }
                    }

                    return false;
                });
            }));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return NeoForgeRegistryLoader.RUN_FEATURE_CANCELLERS_BIOME_MODIFIER.get();
    }
}