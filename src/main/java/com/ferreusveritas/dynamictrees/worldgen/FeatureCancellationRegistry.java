package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.util.holderset.IncludesExcludesHolderSet;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class FeatureCancellationRegistry {
    private static final List<Entry> CANCELLATIONS = new ArrayList<>();

    public static void addCancellations(IncludesExcludesHolderSet<Biome> biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.FeatureCancellations cancellations) {
        CANCELLATIONS.add(new Entry(biomes, operation, cancellations));
    }

    public static List<Entry> getCancellations() {
        return CANCELLATIONS;
    }

    public record Entry(IncludesExcludesHolderSet<Biome> biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.FeatureCancellations cancellations) {}
}
