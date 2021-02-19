package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    boolean shouldCancelFeatures (final ResourceLocation biomeResLoc);

    /**
     * Checks if the given tree feature should be cancelled from the given feature and biome
     * registry names.
     *
     * @param biomeResLoc The resource location of the current biome.
     * @param featureResLoc The resource location of the current tree feature.
     * @return True if we should cancel the feature, false if not.
     */
    boolean shouldCancelFeature (final ResourceLocation biomeResLoc, final ResourceLocation featureResLoc);

    /**
     * Gets the feature cancellers for the given biome.
     *
     * @param biomeResLoc The resource location of the current biome.
     * @return The set of tree feature cancellers to use.
     */
    Set<ITreeFeatureCanceller> getFeatureCancellers (final ResourceLocation biomeResLoc);

    /**
     * Registers default featue canceller for given mod namespace (ID) features for all biomes
     * in the given mod ID.
     *
     * @param modIdForBiomes The mod ID for the biomes.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     */
    default void register(final String modIdForBiomes, final List<String> namespaces) {
        this.register(modIdForBiomes, namespaces, Collections.singletonList(TreeFeatureCancellerRegistry.TREE_CANCELLER));
    }

    /**
     * Registers cancellations for given mod namespace (ID) features for biome based on the
     * given biome registry name.
     *
     * @param biomeResLoc The biome registry name.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     */
    default void register(final ResourceLocation biomeResLoc, final List<String> namespaces) {
        this.register(biomeResLoc, namespaces, Collections.singletonList(TreeFeatureCancellerRegistry.TREE_CANCELLER));
    }

    /**
     * Registers given feature cancellers for given mod namespace (ID) features for all biomes
     * in the given mod ID.
     *
     * @param modIdForBiomes The mod ID for the biomes.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     * @param cancellers The names of the feature cancellers.
     */
    void register(final String modIdForBiomes, final List<String> namespaces, final List<String> cancellers);

    /**
     * Registers given feature cancellers for given mod namespace (ID) features for biome based
     * on the given biome registry name.
     *
     * @param biomeResLoc The biome registry name.
     * @param namespaces A list of mod namespaces (IDs) to remove features from.
     * @param cancellers The names of the feature cancellers.
     */
    void register(final ResourceLocation biomeResLoc, final List<String> namespaces, final List<String> cancellers);

}
