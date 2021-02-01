package com.ferreusveritas.dynamictrees.worldgen.canceller;

import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public interface ITreeCanceller {

    /**
     * Checks if vanilla trees should be cancelled from the given biome registry name.
     *
     * @param biomeResLoc The resource location of the current biome.
     * @return True if we should cancel features in this biome, false if not.
     */
    boolean shouldCancelFeatures (ResourceLocation biomeResLoc);

    /**
     * Checks if the given tree feature should be cancelled from the given feature and biome
     * registry names.
     *
     * @param biomeResLoc The resource location of the current biome.
     * @param featureResLoc The resource location of the current tree feature.
     * @return True if we should cancel the feature, false if not.
     */
    boolean shouldCancelFeature (ResourceLocation biomeResLoc, ResourceLocation featureResLoc);

    /**
     * Registers cancellations for given mod namespace (ID) features for all biomes in the given
     * mod ID.
     *
     * @param modIdForBiomes The mod ID for the biomes.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     */
    void registerCancellations (String modIdForBiomes, List<String> namespaces);

    /**
     * Registers cancellations for given mod namespace (ID) features for biome based on the
     * given biome registry name.
     *
     * @param biomeResLoc The biome registry name.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     */
    void registerCancellations (ResourceLocation biomeResLoc, List<String> namespaces);

}
