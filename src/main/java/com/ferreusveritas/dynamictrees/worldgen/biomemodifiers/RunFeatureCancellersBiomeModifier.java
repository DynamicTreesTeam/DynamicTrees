package com.ferreusveritas.dynamictrees.worldgen.biomemodifiers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.FeatureCancellationRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class RunFeatureCancellersBiomeModifier implements BiomeModifier {
    public static final TagKey<PlacedFeature> FEATURE_CANCELLER_EXCLUSIONS_KEY = TagKey.create(Registry.PLACED_FEATURE_REGISTRY,
            new ResourceLocation(DynamicTrees.MOD_ID, "feature_canceller_exclusions"));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.REMOVE && DTConfigs.WORLD_GEN.get()) {
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

            // final ResourceLocation biomeName = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            //
            // if (biomeName == null) {
            //     return;
            // }
            //

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
    public Codec<? extends BiomeModifier> codec() {
        return DTRegistries.RUN_FEATURE_CANCELLERS_BIOME_MODIFIER.get();
    }
}