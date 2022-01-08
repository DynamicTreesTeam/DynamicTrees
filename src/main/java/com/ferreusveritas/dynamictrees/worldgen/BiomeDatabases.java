package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.event.AddFeatureCancellersEvent;
import com.ferreusveritas.dynamictrees.api.event.PopulateDefaultDatabaseEvent;
import com.ferreusveritas.dynamictrees.api.event.PopulateDimensionalDatabaseEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public final class BiomeDatabases {

    private static final BiomeDatabase DEFAULT_DATABASE = new BiomeDatabase();
    /** Dimension names to their respective {@link com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase}. */
    private static final Map<ResourceLocation, BiomeDatabase> DIMENSIONAL_DATABASES = Maps.newHashMap();
    /** Dimension names for dimensions that are blacklisted. */
    private static final Set<ResourceLocation> BLACKLIST = Sets.newHashSet();

    public static BiomeDatabase getDefault() {
        return DEFAULT_DATABASE;
    }

    public static void reset() {
        DEFAULT_DATABASE.reset();
        DIMENSIONAL_DATABASES.clear();
        BLACKLIST.clear();
    }

    public static BiomeDatabase getDimensionalOrDefault(ResourceLocation dimensionLocation) {
        return Optional.ofNullable(DIMENSIONAL_DATABASES.get(dimensionLocation))
                .orElse(DEFAULT_DATABASE);
    }

    public static BiomeDatabase getOrCreateDimensional(ResourceLocation dimensionLocation) {
        return DIMENSIONAL_DATABASES.computeIfAbsent(dimensionLocation, k -> BiomeDatabase.copyOf(DEFAULT_DATABASE));
    }

    public static void fireAddFeatureCancellersEvent() {
        MinecraftForge.EVENT_BUS.post(new AddFeatureCancellersEvent(DEFAULT_DATABASE));
    }

    public static void firePopulateDefaultDatabaseEvent() {
        MinecraftForge.EVENT_BUS.post(new PopulateDefaultDatabaseEvent(DEFAULT_DATABASE));
    }

    public static void fireDimensionalPopulationEvent() {
        MinecraftForge.EVENT_BUS.post(new PopulateDimensionalDatabaseEvent(DIMENSIONAL_DATABASES, DEFAULT_DATABASE));
    }

    public static boolean isBlacklisted(ResourceLocation dimensionLocation) {
        return BLACKLIST.contains(dimensionLocation);
    }

    public static void populateBlacklistFromConfig() {
        DTConfigs.DIMENSION_BLACKLIST.get().forEach(BiomeDatabases::tryBlacklist);
    }

    private static void tryBlacklist(String location) {
        try {
            BLACKLIST.add(new ResourceLocation(location));
        } catch (ResourceLocationException e) {
            LogManager.getLogger().error("Couldn't get location for dimension blacklist in config.", e);
        }
    }

}
