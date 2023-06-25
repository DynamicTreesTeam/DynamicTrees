package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.Chance;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.ChanceSelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.DensitySelector;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.FeatureCancellation;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.NoFeatureCancellation;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.NormalFeatureCancellation;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelection;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.SpeciesSelector;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.util.holderset.DTBiomeHolderSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class BiomeDatabase {
    private final Map<DTBiomeHolderSet, JsonEntry> jsonEntries = new LinkedHashMap<>();
    private final Map<ResourceLocation, Entry> entries = new HashMap<>();

    public JsonEntry getJsonEntry(DTBiomeHolderSet biomes) {
        return this.jsonEntries.computeIfAbsent(biomes, k -> new JsonEntry(this));
    }

    public Entry getEntry(Holder<Biome> biomeHolder) {
        return this.getEntry(biomeHolder.unwrapKey().orElseThrow());
    }

    public Entry getEntry(ResourceKey<Biome> biomeKey) {
        ResourceLocation biomeRegistryName = biomeKey.location();

        if (this.entries.containsKey(biomeRegistryName))
            return this.entries.get(biomeRegistryName);

        Entry entry = new Entry(this, biomeKey);
        this.entries.put(biomeRegistryName, entry);

        this.jsonEntries.forEach((biomes, jsonEntry) -> {
            if (biomes.containsKey(biomeKey)) {
                // Copy any data explicitly set from json
                jsonEntry.copyTo(entry);
            }
        });

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
     *
     * @implNote does not reset cancellers, since they are only applied once on initial load
     */
    public void reset() {
        this.jsonEntries.clear();
        this.entries.clear();
    }

    public boolean isPopulated() {
        return this.entries.size() > 0;
    }

    public interface EntryReader {
        static DataResult<EntryReader> read(ResourceLocation biomeName) {
            EntryReader entry = BiomeDatabases.getDefault().getEntry(biomeName);
            return DataResult.success(entry);
        }

        @Nullable
        ResourceKey<Biome> getBiomeKey();

        ChanceSelector getChanceSelector();

        DensitySelector getDensitySelector();

        SpeciesSelector getSpeciesSelector();

        FeatureCancellation getFeatureCancellation();

        boolean isBlacklisted();

        float getForestness();

        String getHeightmap();

        Function<Integer, Integer> getMultipass();

    }

    public abstract static class BaseEntry implements EntryReader {
        private final BiomeDatabase database;
        @Nullable
        private final ResourceKey<Biome> biomeKey;
        ChanceSelector chanceSelector = (rnd, spc, rad) -> Chance.UNHANDLED;
        DensitySelector densitySelector = (rnd, nd) -> -1;
        SpeciesSelector speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
        FeatureCancellation featureCancellation = NoFeatureCancellation.INSTANCE;
        Boolean blacklisted = false;
        Float forestness = 0.0f;
        String heightmap = "WORLD_SURFACE_WG";
        private static final Function<Integer, Integer> defaultMultipass = pass -> (pass == 0 ? 0 : -1);
        Function<Integer, Integer> multipass = defaultMultipass;

        public BaseEntry(final BiomeDatabase database, @Nullable final ResourceKey<Biome> biomeKey) {
            this.database = database;
            this.biomeKey = biomeKey;
        }

        public BiomeDatabase getDatabase() {
            return database;
        }

        @Nullable
        @Override
        public ResourceKey<Biome> getBiomeKey() {
            return this.biomeKey;
        }

        @Override
        public ChanceSelector getChanceSelector() {
            return chanceSelector;
        }

        @Override
        public DensitySelector getDensitySelector() {
            return densitySelector;
        }

        @Override
        public SpeciesSelector getSpeciesSelector() {
            return speciesSelector;
        }

        public void setChanceSelector(ChanceSelector chanceSelector) {
            this.chanceSelector = chanceSelector;
        }

        public void setChanceSelector(ChanceSelector selector, Operation op) {
            ChanceSelector existing = chanceSelector;
            switch (op) {
                case REPLACE -> chanceSelector = selector;
                case SPLICE_BEFORE -> chanceSelector = (rnd, spc, rad) -> {
                    Chance c = selector.getChance(rnd, spc, rad);
                    return c != Chance.UNHANDLED ? c : existing.getChance(rnd, spc, rad);
                };
                case SPLICE_AFTER -> chanceSelector = (rnd, spc, rad) -> {
                    Chance c = existing.getChance(rnd, spc, rad);
                    return c != Chance.UNHANDLED ? c : selector.getChance(rnd, spc, rad);
                };
            }
        }

        public void setDensitySelector(DensitySelector densitySelector) {
            this.densitySelector = densitySelector;
        }

        public void setDensitySelector(DensitySelector selector, Operation op) {
            DensitySelector existing = densitySelector;
            switch (op) {
                case REPLACE -> densitySelector = selector;
                case SPLICE_BEFORE -> densitySelector = (rnd, nd) -> {
                    double d = selector.getDensity(rnd, nd);
                    return d >= 0 ? d : existing.getDensity(rnd, nd);
                };
                case SPLICE_AFTER -> densitySelector = (rnd, nd) -> {
                    double d = existing.getDensity(rnd, nd);
                    return d >= 0 ? d : selector.getDensity(rnd, nd);
                };
            }
        }

        public void setSpeciesSelector(SpeciesSelector speciesSelector) {
            this.speciesSelector = speciesSelector;
        }

        public void setSpeciesSelector(SpeciesSelector selector, Operation op) {
            SpeciesSelector existing = speciesSelector;
            switch (op) {
                case REPLACE -> speciesSelector = selector;
                case SPLICE_BEFORE -> speciesSelector = (pos, dirt, rnd) -> {
                    SpeciesSelection ss = selector.getSpecies(pos, dirt, rnd);
                    return ss.isHandled() ? ss : existing.getSpecies(pos, dirt, rnd);
                };
                case SPLICE_AFTER -> speciesSelector = (pos, dirt, rnd) -> {
                    SpeciesSelection ss = existing.getSpecies(pos, dirt, rnd);
                    return ss.isHandled() ? ss : selector.getSpecies(pos, dirt, rnd);
                };
            }
        }

        @Override
        public FeatureCancellation getFeatureCancellation() {
            return featureCancellation;
        }

        /**
         * Gets current feature cancellations or creates a new normal feature cancellation object to store new
         * cancellations on. This should only be used when loading feature cancellation.
         */
        public NormalFeatureCancellation getOrCreateFeatureCancellation() {
            if (featureCancellation == NoFeatureCancellation.INSTANCE) {
                featureCancellation = new NormalFeatureCancellation();
            }
            return ((NormalFeatureCancellation) featureCancellation);
        }

        public void setBlacklisted(boolean blacklisted) {
            this.blacklisted = blacklisted;
        }

        @Override
        public boolean isBlacklisted() {
            return blacklisted;
        }

        public void setForestness(float forestness) {
            this.forestness = forestness;
        }

        public void setHeightmap(String heightmap) {
            this.heightmap = heightmap;
        }

        @Override
        public float getForestness() {
            return forestness;
        }

        @Override
        public String getHeightmap() {
            return heightmap;
        }

        public void setMultipass(Function<Integer, Integer> multipass) {
            this.multipass = multipass;
        }

        @Override
        public Function<Integer, Integer> getMultipass() {
            return multipass;
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
            this.speciesSelector = (pos, dirt, rnd) -> new BiomePropertySelectors.SpeciesSelection();
            this.densitySelector = (rnd, nd) -> -1;
            this.chanceSelector = (rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED;
            this.forestness = 0.0F;
            this.heightmap = "WORLD_SURFACE_WG";
            this.blacklisted = false;
            this.multipass = defaultMultipass;
        }

    }

    public static class Entry extends BaseEntry {
        CaveRootedData caveRootedData;

        public Entry(BiomeDatabase database, @Nullable final ResourceKey<Biome> biomeKey) {
            super(database, biomeKey);
        }

        @Nullable
        public CaveRootedData getCaveRootedData() {
            return this.caveRootedData;
        }

        public boolean hasCaveRootedData() {
            return this.caveRootedData != null;
        }

        public CaveRootedData getOrCreateCaveRootedData() {
            if (!this.hasCaveRootedData())
                this.caveRootedData = new CaveRootedData();

            return this.caveRootedData;
        }

        public void setCaveRootedData(CaveRootedData data) {
            this.caveRootedData = data;
        }

        @Override
        public void reset() {
            super.reset();
            this.caveRootedData = null;
        }
    }

    public static class CaveRootedData {
        /**
         * If {@code true}, the tree will always be generated on the surface. Otherwise, it will be
         * generated on the next available ground position.
         */
        private boolean generateOnSurface = true;

        /**
         * The maximum vertical distance from the cave a tree can generate. Note that this is only
         * checked if {@link #generateOnSurface} is enabled.
         */
        private int maxDistToSurface = 100;

        public boolean shouldGenerateOnSurface() {
            return generateOnSurface;
        }

        public void setGenerateOnSurface(boolean generateOnSurface) {
            this.generateOnSurface = generateOnSurface;
        }

        public int getMaxDistToSurface() {
            return maxDistToSurface;
        }

        public void setMaxDistToSurface(int maxDistToSurface) {
            this.maxDistToSurface = maxDistToSurface;
        }
    }

    /**
     * Holds a biome entry read from treepack JSON that is not tied to a specific biome and is instead
     * deferred and applied later to a set of biome-specific entries.
     */
    public static class JsonEntry extends Entry {
        private boolean force = false;
        private boolean changedChanceSelector = false;
        private boolean changedDensitySelector = false;
        private boolean changedSpeciesSelector = false;
        private boolean changedBlacklisted = false;
        private boolean changedForestness = false;
        private boolean changedHeightmap = false;
        private boolean changedMultipass = false;

        public JsonEntry(BiomeDatabase database) {
            super(database, null);
        }

        /**
         * {@return whether this json entry should be forcefully copied and reset any other properties on all declared biomes}
         * Defaults to false.
         */
        public boolean isForce() {
            return this.force;
        }

        /**
         * Sets whether this json entry should be forcefully copied and reset any other properties on all declared biomes.
         */
        public void setForce(boolean force) {
            this.force = force;
        }

        /**
         * Copies the changed data stored in this JSON entry to the specified {@code other} entry.
         * This copy method does not overwrite properties in {@code other} that were not changed by this JSON entry.
         *
         * @param other the other entry
         */
        public void copyTo(Entry other) {
            if (this.force)
                other.reset();
            if (this.changedChanceSelector)
                other.chanceSelector = this.chanceSelector;
            if (this.changedDensitySelector)
                other.densitySelector = this.densitySelector;
            if (this.changedSpeciesSelector)
                other.speciesSelector = this.speciesSelector;
            if (this.changedBlacklisted)
                other.blacklisted = this.blacklisted;
            if (this.changedForestness)
                other.forestness = this.forestness;
            if (this.changedHeightmap)
                other.heightmap = this.heightmap;
            if (this.changedMultipass)
                other.multipass = this.multipass;
            if (this.caveRootedData != null)
                other.caveRootedData = this.caveRootedData;
        }

        @Override
        public void setChanceSelector(ChanceSelector chanceSelector) {
            this.changedChanceSelector = true;
            super.setChanceSelector(chanceSelector);
        }

        @Override
        public void setChanceSelector(ChanceSelector selector, Operation op) {
            this.changedChanceSelector = true;
            super.setChanceSelector(selector, op);
        }

        @Override
        public void setDensitySelector(DensitySelector densitySelector) {
            this.changedDensitySelector = true;
            super.setDensitySelector(densitySelector);
        }

        @Override
        public void setDensitySelector(DensitySelector selector, Operation op) {
            this.changedDensitySelector = true;
            super.setDensitySelector(selector, op);
        }

        @Override
        public void setSpeciesSelector(SpeciesSelector speciesSelector) {
            this.changedSpeciesSelector = true;
            super.setSpeciesSelector(speciesSelector);
        }

        @Override
        public void setSpeciesSelector(SpeciesSelector selector, Operation op) {
            this.changedSpeciesSelector = true;
            super.setSpeciesSelector(selector, op);
        }

        @Override
        public void setBlacklisted(boolean blacklisted) {
            this.changedBlacklisted = true;
            super.setBlacklisted(blacklisted);
        }

        @Override
        public void setForestness(float forestness) {
            this.changedForestness = true;
            super.setForestness(forestness);
        }

        @Override
        public void setHeightmap(String heightmap) {
            this.changedHeightmap = true;
            super.setHeightmap(heightmap);
        }

        @Override
        public void setMultipass(Function<Integer, Integer> multipass) {
            this.changedMultipass = true;
            super.setMultipass(multipass);
        }

        @Override
        public void setCustomMultipass(JsonObject json) {
            this.changedMultipass = true;
            super.setCustomMultipass(json);
        }
    }

    public SpeciesSelector getSpecies(Holder<Biome> biome) {
        return getEntry(biome).getSpeciesSelector();
    }

    public ChanceSelector getChance(Holder<Biome> biome) {
        return getEntry(biome).getChanceSelector();
    }

    public DensitySelector getDensitySelector(Holder<Biome> biome) {
        return getEntry(biome).getDensitySelector();
    }

    public float getForestness(Holder<Biome> biome) {
        return getEntry(biome).getForestness();
    }

    public String getHeightmap(Holder<Biome> biome) {
        return getEntry(biome).getHeightmap();
    }

    public Function<Integer, Integer> getMultipass(Holder<Biome> biome) {
        return getEntry(biome).getMultipass();
    }

    public BiomeDatabase setForestness(Holder<Biome> biome, float forestness) {
        getEntry(biome).setForestness((float) Math.max(forestness, DTConfigs.SEED_MIN_FORESTNESS.get()));
        return this;
    }

    public BiomeDatabase setHeightmap(Holder<Biome> biome, String heightmap) {
        getEntry(biome).setHeightmap(heightmap);
        return this;
    }

    public BiomeDatabase setMultipass(Holder<Biome> biome, Function<Integer, Integer> multipass) {
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
        databaseCopy.jsonEntries.putAll(database.jsonEntries);
        databaseCopy.entries.putAll(database.entries);
        return databaseCopy;
    }

}
