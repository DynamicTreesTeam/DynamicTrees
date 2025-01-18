package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;

import java.util.ArrayList;
import java.util.List;

public class FeatureCancellationRegistry {
    private static final List<Entry> CANCELLATIONS = new ArrayList<>();

    public static void addCancellations(IDTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.NormalFeatureCancellation cancellations) {
        CANCELLATIONS.add(new Entry(biomes, operation, cancellations));
    }

    public static List<Entry> getCancellations() {
        return CANCELLATIONS;
    }

    public record Entry(IDTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.NormalFeatureCancellation cancellations) {}
}