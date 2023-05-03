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
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BiomeDatabase {

    public static final Entry BAD_ENTRY = new Entry() {
        @Override
        public void setChanceSelector(ChanceSelector chanceSelector) {
        }

        @Override
        public void setDensitySelector(DensitySelector densitySelector) {
        }

        @Override
        public void setSpeciesSelector(SpeciesSelector speciesSelector) {
        }
    };

    private final Map<ResourceLocation, Entry> entries = new HashMap<>();

    public Entry getEntry(@Nullable Holder<Biome> biome) {
		if (biome == null) {
			return BAD_ENTRY;
		}

        return this.entries.computeIfAbsent(ForgeRegistries.BIOMES.getKey(biome.get()), k -> new Entry(this, biome));
    }

    public Entry getEntry(ResourceLocation biomeResLoc) {
        return this.getEntry(ForgeRegistries.BIOMES.getHolder(biomeResLoc).get());
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
        this.entries.values().forEach(BaseEntry::reset);
    }

    public void clear() {
        this.entries.clear();
    }

//    public boolean isValid() {
//        for (Biome biome : ForgeRegistries.BIOMES) {
//            final BaseEntry entry = this.getEntry(Holder.direct(biome));
//            final ResourceLocation biomeRegistryName = ForgeRegistries.BIOMES.getKey(entry.getBiome().get());
//
//            if (biomeRegistryName != null && !biomeRegistryName.equals(ForgeRegistries.BIOMES.getKey(biome))) {
//                return false;
//            }
//        }
//
//        return true;
//    }

    public boolean isPopulated() {
        return this.entries.size() > 0;
    }

    public interface EntryReader {
        Codec<EntryReader> CODEC = ResourceLocation.CODEC.comapFlatMap(EntryReader::read, entry -> ForgeRegistries.BIOMES.getKey(entry.getBiome().get()));

        static DataResult<EntryReader> read(ResourceLocation biomeName) {
            EntryReader entry = BiomeDatabases.getDefault().getEntry(biomeName);
            if (entry == BAD_ENTRY) {
                return DataResult.error("Could not get entry from name: " + biomeName);
            }
            return DataResult.success(entry);
        }

        Holder<Biome> getBiome();

        ChanceSelector getChanceSelector();

        DensitySelector getDensitySelector();

        SpeciesSelector getSpeciesSelector();

        FeatureCancellation getFeatureCancellation();

        boolean isBlacklisted();

        float getForestness();

        String getHeightmap();

        Function<Integer, Integer> getMultipass();

    }

    public static abstract class BaseEntry implements EntryReader {
        private final BiomeDatabase database;
        private final Holder<Biome> biome;
        private ChanceSelector chanceSelector = (rnd, spc, rad) -> Chance.UNHANDLED;
        private DensitySelector densitySelector = (rnd, nd) -> -1;
        private SpeciesSelector speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
        private FeatureCancellation featureCancellation = NoFeatureCancellation.INSTANCE;
        private boolean blacklisted = false;
        private float forestness = 0.0f;
        private String heightmap = "WORLD_SURFACE_WG";
        private final static Function<Integer, Integer> defaultMultipass = pass -> (pass == 0 ? 0 : -1);
        private Function<Integer, Integer> multipass = defaultMultipass;

        public BaseEntry() {
            this.database = null;
            this.biome = ForgeRegistries.BIOMES.getHolder(Biomes.OCEAN).get();
        }

        public BaseEntry(final BiomeDatabase database, final Holder<Biome> biome) {
            this.database = database;
            this.biome = biome;
        }

        public BiomeDatabase getDatabase() {
            return database;
        }

        @Override
        public Holder<Biome> getBiome() {
            return biome;
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

        private CaveRootedEntry caveRootedEntry;

        public Entry() {
            super();
        }

        public Entry(BiomeDatabase database, Holder<Biome> biome) {
            super(database, biome);
        }

        public CaveRootedEntry getCaveRootedEntry() {
            return caveRootedEntry;
        }

        public boolean hasCaveRootedEntry() {
            return caveRootedEntry != null;
        }

        public CaveRootedEntry getOrCreateCaveRootedEntry() {
            if (!hasCaveRootedEntry()) {
                caveRootedEntry = new CaveRootedEntry();
            }
            return caveRootedEntry;
        }

        public void setCaveRootedEntry(CaveRootedEntry entry) {
            this.caveRootedEntry = entry;
        }

        @Override
        public void reset() {
            super.reset();
            caveRootedEntry = null;
        }
    }

    public static class CaveRootedEntry extends BaseEntry {

        /**
         * If {@code true}, the tree will always be generated on the surface. Otherwise it will be
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
        databaseCopy.entries.putAll(database.entries);
        return databaseCopy;
    }

}
