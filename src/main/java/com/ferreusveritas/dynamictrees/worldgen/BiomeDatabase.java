package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.*;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    public Entry getEntry(@Nullable Biome biome) {
		if (biome == null) {
			return BAD_ENTRY;
		}

        return this.entries.computeIfAbsent(biome.getRegistryName(), k -> new Entry(this, biome));
    }

    public Entry getEntry(ResourceLocation biomeResLoc) {
        return this.getEntry(ForgeRegistries.BIOMES.getValue(biomeResLoc));
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
        this.entries.values().forEach(Entry::reset);
    }

    public void clear() {
        this.entries.clear();
    }

    public boolean isValid() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            final Entry entry = this.getEntry(biome);
            final ResourceLocation biomeRegistryName = entry.getBiome().getRegistryName();

            if (biomeRegistryName != null && !biomeRegistryName.equals(biome.getRegistryName())) {
                return false;
            }
        }

        return true;
    }

    public boolean isPopulated() {
        return this.entries.size() > 0;
    }

    public static class Entry {
        private final BiomeDatabase database;
        private final Biome biome;
        private ChanceSelector chanceSelector = (rnd, spc, rad) -> Chance.UNHANDLED;
        private DensitySelector densitySelector = (rnd, nd) -> -1;
        private SpeciesSelector speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
        private FeatureCancellation featureCancellation = NoFeatureCancellation.INSTANCE;
        private boolean blacklisted = false;
        private float forestness = 0.0f;
        private final static Function<Integer, Integer> defaultMultipass = pass -> (pass == 0 ? 0 : -1);
        private Function<Integer, Integer> multipass = defaultMultipass;

        public Entry() {
            this.database = null;
            this.biome = ForgeRegistries.BIOMES.getValue(Biomes.OCEAN.getRegistryName());
        }

        public Entry(final BiomeDatabase database, final Biome biome) {
            this.database = database;
            this.biome = biome;
        }

        public BiomeDatabase getDatabase() {
            return database;
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

        public boolean isBlacklisted() {
            return blacklisted;
        }

        public void setForestness(float forestness) {
            this.forestness = forestness;
        }

        public float getForestness() {
            return forestness;
        }

        public void setMultipass(Function<Integer, Integer> multipass) {
            this.multipass = multipass;
        }

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
            this.blacklisted = false;
            this.multipass = defaultMultipass;
        }

    }

    public SpeciesSelector getSpecies(Biome biome) {
        return getEntry(biome).speciesSelector;
    }

    public ChanceSelector getChance(Biome biome) {
        return getEntry(biome).chanceSelector;
    }

    public DensitySelector getDensitySelector(Biome biome) {
        return getEntry(biome).densitySelector;
    }

    public float getForestness(Biome biome) {
        return getEntry(biome).getForestness();
    }

    public Function<Integer, Integer> getMultipass(Biome biome) {
        return getEntry(biome).getMultipass();
    }

    public BiomeDatabase setSpeciesSelector(final Biome biome, @Nullable final SpeciesSelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
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

    public BiomeDatabase setChanceSelector(final Biome biome, @Nullable final ChanceSelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
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

    public BiomeDatabase setDensitySelector(final Biome biome, @Nullable final DensitySelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
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

    public BiomeDatabase setForestness(Biome biome, float forestness) {
        getEntry(biome).setForestness((float) Math.max(forestness, DTConfigs.SEED_MIN_FORESTNESS.get()));
        return this;
    }

    public BiomeDatabase setMultipass(Biome biome, Function<Integer, Integer> multipass) {
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
