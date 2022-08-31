package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.Chance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.DensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.GroundFinder;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.util.holderset.IncludesExcludesHolderSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class BiomeDatabase {

    private final Map<IncludesExcludesHolderSet<Biome>, Entry> jsonEntries = new LinkedHashMap<>();
    private final Map<ResourceLocation, Entry> entries = new HashMap<>();

    public Entry getJsonEntry(IncludesExcludesHolderSet<Biome> biomes) {
        return this.jsonEntries.computeIfAbsent(biomes, k -> new Entry(this, null, null));
    }

    public Entry getEntry(Holder<Biome> biomeHolder) {
        ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().orElseThrow();
        ResourceLocation biomeRegistryName = biomeKey.location();

        if (this.entries.containsKey(biomeRegistryName))
            return this.entries.get(biomeRegistryName);

        Entry entry = new Entry(this, biomeKey, biomeHolder.value());
        this.entries.put(biomeRegistryName, entry);

        for (Map.Entry<IncludesExcludesHolderSet<Biome>, Entry> jsonEntry : this.jsonEntries.entrySet()) {
            if (jsonEntry.getKey().contains(biomeHolder)) {
                // Copy any data explicitly set from json
                entry.copyFrom(jsonEntry.getValue());
            }
        }

        // Finally, initialize any defaults that were not already set
        entry.initializeDefaults();

        return entry;
    }

    public Entry getEntry(ResourceLocation biomeResLoc) {
        return this.entries.get(biomeResLoc);
    }

    public Collection<Entry> getAllEntries() {
        return this.entries.values();
    }

    /**
     * Resets all entries in the database.
     */
    public void reset() {
        this.entries.values().forEach(Entry::reset);
    }

    public void clear() {
        this.entries.clear();
    }

    // public boolean isValid() {
    //     for (var registryEntry : ForgeRegistries.BIOMES.getEntries()) {
    //         final Entry entry = this.getEntry(registryEntry.getValue());
    //         final ResourceLocation biomeRegistryName = ForgeRegistries.BIOMES.getKey(entry.getBiome());
    //
    //         if (biomeRegistryName != null && !biomeRegistryName.equals(registryEntry.getKey().location())) {
    //             return false;
    //         }
    //     }
    //
    //     return true;
    // }

    public boolean isPopulated() {
        return !this.entries.isEmpty();
    }

    public static class Entry {
        private final BiomeDatabase database;
        private final ResourceKey<Biome> biomeKey;
        private final Biome biome;
        private ChanceSelector chanceSelector;
        private DensitySelector densitySelector;
        private SpeciesSelector speciesSelector;
        private Boolean blacklisted;
        private Boolean subterranean;
        private Float forestness;
        private static final IntUnaryOperator defaultMultipass = pass -> (pass == 0 ? 0 : -1);
        private IntUnaryOperator multipass;
        private GroundFinder groundFinder;

        public Entry(final BiomeDatabase database, final ResourceKey<Biome> biomeKey, final Biome biome) {
            this.database = database;
            this.biomeKey = biomeKey;
            this.biome = biome;
        }

        public void initializeDefaults() {
            if (this.chanceSelector == null)
                this.chanceSelector = (rnd, spc, rad) -> Chance.UNHANDLED;
            if (this.densitySelector == null)
                this.densitySelector = (rnd, nd) -> -1;
            if (this.speciesSelector == null)
                this.speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
            if (this.blacklisted == null)
                this.blacklisted = false;
            if (this.subterranean == null)
                this.subterranean = false;
            if (this.forestness == null)
                this.forestness = 0.0f;
            if (this.multipass == null)
                this.multipass = defaultMultipass;
            if (this.groundFinder == null)
                this.groundFinder = GroundFinder.OVERWORLD;
        }

        public void copyFrom(Entry entry) {
            if (entry.chanceSelector != null)
                this.chanceSelector = entry.chanceSelector;
            if (entry.densitySelector != null)
                this.densitySelector = entry.densitySelector;
            if (entry.speciesSelector != null)
                this.speciesSelector = entry.speciesSelector;
            if (entry.blacklisted != null)
                this.blacklisted = entry.blacklisted;
            if (entry.subterranean != null)
                this.subterranean = entry.subterranean;
            if (entry.forestness != null)
                this.forestness = entry.forestness;
            if (entry.multipass != null)
                this.multipass = entry.multipass;
            if (entry.groundFinder != null)
                this.groundFinder = entry.groundFinder;
        }

        public BiomeDatabase getDatabase() {
            return database;
        }

        public ResourceKey<Biome> getBiomeKey() {
            return biomeKey;
        }

        public Biome getBiome() {
            return biome;
        }

        public ChanceSelector getChanceSelector() {
            return chanceSelector;
        }

        public DensitySelector getDensitySelector() {
            return densitySelector;
        }

        public SpeciesSelector getSpeciesSelector() {
            return speciesSelector;
        }

        public void setChanceSelector(ChanceSelector chanceSelector) {
            this.chanceSelector = chanceSelector;
        }

        public void setDensitySelector(DensitySelector densitySelector) {
            this.densitySelector = densitySelector;
        }

        public void setSpeciesSelector(SpeciesSelector speciesSelector) {
            this.speciesSelector = speciesSelector;
        }

        public void setBlacklisted(boolean blacklisted) {
            this.blacklisted = blacklisted;
        }

        public void setSubterranean(boolean is) {
            this.subterranean = is;
            this.groundFinder = is ? GroundFinder.SUBTERRANEAN : GroundFinder.OVERWORLD;
        }

        public boolean isBlacklisted() {
            return blacklisted;
        }

        public boolean isSubterranean() {
            return subterranean;
        }

        public void setForestness(float forestness) {
            this.forestness = forestness;
        }

        public float getForestness() {
            return forestness;
        }

        public void setMultipass(IntUnaryOperator multipass) {
            this.multipass = multipass;
        }

        public IntUnaryOperator getMultipass() {
            return multipass;
        }

        public GroundFinder getGroundFinder() {
            return groundFinder;
        }

        public void setGroundFinder(GroundFinder groundFinder) {
            this.groundFinder = groundFinder;
        }

        public void enableDefaultMultipass() {
            this.multipass = pass -> {
                switch (pass) {
                    case 0:
                        return 0; // Zero means to run as normal.
                    case 1:
                        return 5; // Return only radius 5 on pass 1.
                    case 2:
                        return 3; // Return only radius 3 on pass 2.
                    default:
                        return -1; // A negative number means to terminate.
                }
            };
        }

        public void setCustomMultipass(JsonObject json) {
            final Map<Integer, Integer> passMap = this.deserialiseCustomMultipass(json);
            this.multipass = pass -> passMap.getOrDefault(pass, -1);
        }

        private Map<Integer, Integer> deserialiseCustomMultipass(JsonObject json) {
            final Map<Integer, Integer> passMap = Maps.newHashMap();

            for (final Map.Entry<String, JsonElement> passEntry : json.entrySet()) {
                try {
                    final int pass = Integer.parseInt(passEntry.getKey());
                    final int radius = JsonDeserialisers.INTEGER.deserialise(passEntry.getValue())
                            .orElse(-1);

                    // Terminate when radius is -1.
                    if (radius == -1) {
                        break;
                    }

                    passMap.put(pass, radius);
                } catch (NumberFormatException ignored) {
                }
            }
            return passMap;
        }

        public void reset() {
            this.chanceSelector = (rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED;
            this.densitySelector = (rnd, nd) -> -1;
            this.speciesSelector = (pos, dirt, rnd) -> new BiomePropertySelectors.SpeciesSelection();
            this.forestness = 0.0F;
            this.blacklisted = false;
            this.subterranean = false;
            this.multipass = defaultMultipass;
        }

    }

    public SpeciesSelector getSpecies(Holder<Biome> biome) {
        return getEntry(biome).speciesSelector;
    }

    public ChanceSelector getChance(Holder<Biome> biome) {
        return getEntry(biome).chanceSelector;
    }

    public DensitySelector getDensitySelector(Holder<Biome> biome) {
        return getEntry(biome).densitySelector;
    }

    public float getForestness(Holder<Biome> biome) {
        return getEntry(biome).getForestness();
    }

    public IntUnaryOperator getMultipass(Holder<Biome> biome) {
        return getEntry(biome).getMultipass();
    }

    public BiomeDatabase setSpeciesSelector(final Entry entry, @Nullable final SpeciesSelector selector, final Operation op) {
        if (selector == null) {
            return this;
        }

        final SpeciesSelector existing = entry.getSpeciesSelector();

        switch (op) {
            case REPLACE:
                entry.setSpeciesSelector(selector);
                break;
            case SPLICE_BEFORE:
                entry.setSpeciesSelector((pos, dirt, rnd) -> {
                    SpeciesSelection ss = selector.getSpecies(pos, dirt, rnd);
                    return ss.isHandled() ? ss : existing.getSpecies(pos, dirt, rnd);
                });
                break;
            case SPLICE_AFTER:
                entry.setSpeciesSelector((pos, dirt, rnd) -> {
                    SpeciesSelection ss = existing.getSpecies(pos, dirt, rnd);
                    return ss.isHandled() ? ss : selector.getSpecies(pos, dirt, rnd);
                });
                break;
        }

        return this;
    }

    public BiomeDatabase setChanceSelector(final Entry entry, @Nullable final ChanceSelector selector, final Operation op) {
        if (selector == null) {
            return this;
        }

        final ChanceSelector existing = entry.getChanceSelector();

        switch (op) {
            case REPLACE:
                entry.setChanceSelector(selector);
                break;
            case SPLICE_BEFORE:
                entry.setChanceSelector((rnd, spc, rad) -> {
                    Chance c = selector.getChance(rnd, spc, rad);
                    return c != Chance.UNHANDLED ? c : existing.getChance(rnd, spc, rad);
                });
                break;
            case SPLICE_AFTER:
                entry.setChanceSelector((rnd, spc, rad) -> {
                    Chance c = existing.getChance(rnd, spc, rad);
                    return c != Chance.UNHANDLED ? c : selector.getChance(rnd, spc, rad);
                });
                break;
        }

        return this;
    }

    public BiomeDatabase setDensitySelector(final Entry entry, @Nullable final DensitySelector selector, final Operation op) {
        if (selector == null) {
            return this;
        }

        final DensitySelector existing = entry.getDensitySelector();

        switch (op) {
            case REPLACE:
                entry.setDensitySelector(selector);
                break;
            case SPLICE_BEFORE:
                entry.setDensitySelector((rnd, nd) -> {
                    double d = selector.getDensity(rnd, nd);
                    return d >= 0 ? d : existing.getDensity(rnd, nd);
                });
                break;
            case SPLICE_AFTER:
                entry.setDensitySelector((rnd, nd) -> {
                    double d = existing.getDensity(rnd, nd);
                    return d >= 0 ? d : selector.getDensity(rnd, nd);
                });
                break;
        }

        return this;
    }

    public BiomeDatabase setIsSubterranean(Holder<Biome> biome, boolean is) {
        getEntry(biome).setSubterranean(is);
        return this;
    }

    public BiomeDatabase setForestness(Holder<Biome> biome, float forestness) {
        getEntry(biome).setForestness((float) Math.max(forestness, DTConfigs.SEED_MIN_FORESTNESS.get()));
        return this;
    }

    public BiomeDatabase setMultipass(Holder<Biome> biome, IntUnaryOperator multipass) {
        getEntry(biome).setMultipass(multipass);
        return this;
    }

    public enum Operation {
        REPLACE,
        SPLICE_BEFORE,
        SPLICE_AFTER
    }

    public static BiomeDatabase copyOf(final BiomeDatabase database) {
        final BiomeDatabase databaseCopy = new BiomeDatabase();
        databaseCopy.entries.putAll(database.entries);
        return databaseCopy;
    }

}
