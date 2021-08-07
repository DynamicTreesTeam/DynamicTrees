package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors.*;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BiomeDatabase {

    public static final Entry BAD_ENTRY = new Entry() {
        @Override
        public void setChanceSelector(IChanceSelector chanceSelector) {
        }

        @Override
        public void setDensitySelector(IDensitySelector densitySelector) {
        }

        @Override
        public void setSpeciesSelector(ISpeciesSelector speciesSelector) {
        }

        @Override
        public void setSubterraneanBiome(boolean is) {
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
        private IChanceSelector chanceSelector = (rnd, spc, rad) -> Chance.UNHANDLED;
        private IDensitySelector densitySelector = (rnd, nd) -> -1;
        private ISpeciesSelector speciesSelector = (pos, dirt, rnd) -> new SpeciesSelection();
        private final FeatureCancellations featureCancellations = new FeatureCancellations();
        private boolean blacklisted = false;
        private boolean isSubterranean = false;
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

        public IChanceSelector getChanceSelector() {
            return chanceSelector;
        }

        public IDensitySelector getDensitySelector() {
            return densitySelector;
        }

        public ISpeciesSelector getSpeciesSelector() {
            return speciesSelector;
        }

        public void setChanceSelector(IChanceSelector chanceSelector) {
            this.chanceSelector = chanceSelector;
        }

        public void setDensitySelector(IDensitySelector densitySelector) {
            this.densitySelector = densitySelector;
        }

        public void setSpeciesSelector(ISpeciesSelector speciesSelector) {
            this.speciesSelector = speciesSelector;
        }

        public FeatureCancellations getFeatureCancellations() {
            return featureCancellations;
        }

        public void setBlacklisted(boolean blacklisted) {
            this.blacklisted = blacklisted;
        }

        public void setSubterraneanBiome(boolean is) {
            this.isSubterranean = is;
        }

        public boolean isBlacklisted() {
            return blacklisted;
        }

        public boolean isSubterraneanBiome() {
            return isSubterranean;
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

    }

    public ISpeciesSelector getSpecies(Biome biome) {
        return getEntry(biome).speciesSelector;
    }

    public IChanceSelector getChance(Biome biome) {
        return getEntry(biome).chanceSelector;
    }

    public IDensitySelector getDensitySelector(Biome biome) {
        return getEntry(biome).densitySelector;
    }

    public float getForestness(Biome biome) {
        return getEntry(biome).getForestness();
    }

    public Function<Integer, Integer> getMultipass(Biome biome) {
        return getEntry(biome).getMultipass();
    }

    public BiomeDatabase setSpeciesSelector(final Biome biome, @Nullable final ISpeciesSelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
        final ISpeciesSelector existing = entry.getSpeciesSelector();

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

    public BiomeDatabase setChanceSelector(final Biome biome, @Nullable final IChanceSelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
        final IChanceSelector existing = entry.getChanceSelector();

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

    public BiomeDatabase setDensitySelector(final Biome biome, @Nullable final IDensitySelector selector, final Operation op) {
		if (selector == null) {
			return this;
		}

        final Entry entry = getEntry(biome);
        final IDensitySelector existing = entry.getDensitySelector();

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

    public BiomeDatabase setIsSubterranean(Biome biome, boolean is) {
        getEntry(biome).setSubterraneanBiome(is);
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
