package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.util.holderset.DTBiomeHolderSet;

import java.util.ArrayList;
import java.util.List;

public class FeatureCancellationRegistry {
    private static final List<Entry> CANCELLATIONS = new ArrayList<>();

    public static void addCancellations(DTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.NormalFeatureCancellation cancellations) {
        CANCELLATIONS.add(new Entry(biomes, operation, cancellations));
    }

    public static List<Entry> getCancellations() {
        return CANCELLATIONS;
    }

    public record Entry(DTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.NormalFeatureCancellation cancellations) {}
}