package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.worldgen.feature.CaveRootedTreePlacement;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;

import java.util.List;

/**
 * DT's own worldgen features. Village tree replacement and nylium-fungi swap JSON
 * is kept in {@code common/src/generated/server/resources} from prior datagen; RegistrySetBuilder
 * cannot look up vanilla template pools during bootstrap on 26.2.
 */
public final class DTDatapackBuiltinEntriesProvider {

    private DTDatapackBuiltinEntriesProvider() {}

    public static RegistrySetBuilder registries() {
        return new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, DTDatapackBuiltinEntriesProvider::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, DTDatapackBuiltinEntriesProvider::bootstrapPlacedFeatures);
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(DTRegistries.DYNAMIC_TREE_CONFIGURED_FEATURE,
                new ConfiguredFeature<>(DTRegistries.DYNAMIC_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(DTRegistries.CAVE_ROOTED_TREE_CONFIGURED_FEATURE,
                new ConfiguredFeature<>(DTRegistries.CAVE_ROOTED_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(DTRegistries.DYNAMIC_TREE_PLACED_FEATURE,
                new PlacedFeature(configuredFeatures.getOrThrow(DTRegistries.DYNAMIC_TREE_CONFIGURED_FEATURE), List.of()));
        context.register(DTRegistries.CAVE_ROOTED_TREE_PLACED_FEATURE,
                new PlacedFeature(configuredFeatures.getOrThrow(DTRegistries.CAVE_ROOTED_TREE_CONFIGURED_FEATURE), List.of(
                        CaveRootedTreePlacement.INSTANCE, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                        EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome())));
    }
}
