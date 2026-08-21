package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public final class BiomeDatabases {

    private static final BiomeDatabase DEFAULT_DATABASE = new BiomeDatabase();
    /** Dimension names to their respective {@link com.dtteam.dynamictrees.worldgen.BiomeDatabase}. */
    private static final Map<Identifier, BiomeDatabase> DIMENSIONAL_DATABASES = Maps.newConcurrentMap();
    /** Dimension names for dimensions that are blacklisted. */
    private static final Set<Identifier> BLACKLIST = Sets.newConcurrentHashSet();

    public static BiomeDatabase getDefault() {
        return DEFAULT_DATABASE;
    }

    public static BiomeDatabase getDimensionalOrDefault(Identifier dimensionLocation) {
        return Optional.ofNullable(DIMENSIONAL_DATABASES.get(dimensionLocation))
                .orElse(DEFAULT_DATABASE);
    }

    public static BiomeDatabase getOrCreateDimensional(Identifier dimensionLocation) {
        return DIMENSIONAL_DATABASES.computeIfAbsent(dimensionLocation, k -> BiomeDatabase.copyOf(DEFAULT_DATABASE));
    }

    public static Map<Identifier, BiomeDatabase> getDimensionalDatabases() {
        return DIMENSIONAL_DATABASES;
    }

    public static boolean isBlacklisted(Identifier dimensionLocation) {
        return BLACKLIST.contains(dimensionLocation);
    }

    public static void populateBlacklistFromConfig() {
        if (DTConfigs.SERVER_CONFIG.isLoaded()){
            DTConfigs.SERVER.dimensionBlacklist.get().forEach(BiomeDatabases::tryBlacklist);
        } else {
            LogManager.getLogger().error("Dimension Blacklist tried to load from config before config was loaded! this should not happen.");
        }
    }

    private static void tryBlacklist(String location) {
        try {
            BLACKLIST.add(Identifier.parse(location));
        } catch (IdentifierException e) {
            LogManager.getLogger().error("Couldn't get location for dimension blacklist in config.", e);
        }
    }

    public static void reset() {
        DEFAULT_DATABASE.reset();
        DIMENSIONAL_DATABASES.clear();
        BLACKLIST.clear();
    }

}
