package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DTFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> DYNAMIC_TREE_CONFIGURED_FEATURE = configuredFeature("dynamic_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_ROOTED_TREE_CONFIGURED_FEATURE = configuredFeature("cave_rooted_tree");
    public static final ResourceKey<PlacedFeature> DYNAMIC_TREE_PLACED_FEATURE = placedFeature("dynamic_tree");
    /**
     * Placement for trees that generate on the surface above the target biome. This is used for trees like the azalea.
     */
    public static final ResourceKey<PlacedFeature> CAVE_ROOTED_TREE_PLACED_FEATURE = placedFeature("cave_rooted_tree");

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredFeature(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, DynamicTrees.location(name));
    }

    private static ResourceKey<PlacedFeature> placedFeature(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, DynamicTrees.location(name));
    }
}
