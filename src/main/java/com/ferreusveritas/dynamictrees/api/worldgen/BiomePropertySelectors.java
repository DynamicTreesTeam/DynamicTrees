package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Provides the forest density for a given biome. Mods should implement this interface and register it via the {@link
 * TreeRegistry} to control how densely populated a {@link net.minecraft.world.level.biome.Biome} is.
 *
 * @author ferreusveritas
 */
public class BiomePropertySelectors {

    @FunctionalInterface
    public interface ChanceSelector {
        Chance getChance(Random random, @Nonnull Species species, int radius);
    }

    @FunctionalInterface
    public interface DensitySelector {
        double getDensity(Random random, double noiseDensity);
    }

    @FunctionalInterface
    public interface SpeciesSelector {
        SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, Random random);
    }

    public static final class FeatureCancellations {
        private final Collection<String> namespaces = Sets.newHashSet();
        private final Collection<FeatureCanceller> featureCancellers = Sets.newHashSet();
        private final Collection<GenerationStep.Decoration> stages = Sets.newHashSet();

        public void putNamespace(final String namespace) {
            this.namespaces.add(namespace);
        }

        public boolean shouldCancelNamespace(final String namespace) {
            return this.namespaces.contains(namespace);
        }

        public void putCanceller(final FeatureCanceller featureCanceller) {
            this.featureCancellers.add(featureCanceller);
        }

        public void putStage(final GenerationStep.Decoration stage) {
            this.stages.add(stage);
        }

        public void putDefaultStagesIfEmpty() {
            if (this.stages.size() < 1) {
                this.stages.add(GenerationStep.Decoration.VEGETAL_DECORATION);
            }
        }

        public void addAllFrom(final FeatureCancellations featureCancellations) {
            this.namespaces.addAll(featureCancellations.namespaces);
            this.featureCancellers.addAll(featureCancellations.featureCancellers);
            this.stages.addAll(featureCancellations.stages);
        }

        public void reset() {
            this.namespaces.clear();
            this.featureCancellers.clear();
            this.stages.clear();
        }

        public Collection<FeatureCanceller> getFeatureCancellers() {
            return this.featureCancellers;
        }

        public Collection<GenerationStep.Decoration> getStages() {
            return this.stages;
        }

    }

    /**
     * This is the data that represents a species selection. This class was necessary to have an unhandled state.
     */
    public static class SpeciesSelection {
        private final boolean handled;
        private final Species species;

        public SpeciesSelection() {
            handled = false;
            species = Species.NULL_SPECIES;
        }

        public SpeciesSelection(@Nonnull Species species) {
            this.species = species;
            handled = true;
        }

        public boolean isHandled() {
            return handled;
        }

        public Species getSpecies() {
            return species;
        }
    }

    public static class StaticSpeciesSelector implements SpeciesSelector {
        final SpeciesSelection decision;

        public StaticSpeciesSelector(SpeciesSelection decision) {
            this.decision = decision;
        }

        public StaticSpeciesSelector(@Nonnull Species species) {
            this(new SpeciesSelection(species));
        }

        public StaticSpeciesSelector() {
            this(new SpeciesSelection());
        }

        @Override
        public SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, Random random) {
            return decision;
        }
    }

    public static class RandomSpeciesSelector implements SpeciesSelector {

        private class Entry {
            public Entry(SpeciesSelection d, int w) {
                decision = d;
                weight = w;
            }

            public SpeciesSelection decision;
            public int weight;
        }

        ArrayList<Entry> decisionTable = new ArrayList<Entry>();
        int totalWeight;

        public int getSize() {
            return decisionTable.size();
        }

        public RandomSpeciesSelector add(@Nonnull Species species, int weight) {
            decisionTable.add(new Entry(new SpeciesSelection(species), weight));
            totalWeight += weight;
            return this;
        }

        public RandomSpeciesSelector add(int weight) {
            decisionTable.add(new Entry(new SpeciesSelection(), weight));
            totalWeight += weight;
            return this;
        }

        @Override
        public SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, Random random) {
            int chance = random.nextInt(totalWeight);

            for (Entry entry : decisionTable) {
                if (chance < entry.weight) {
                    return entry.decision;
                }
                chance -= entry.weight;
            }

            return decisionTable.get(decisionTable.size() - 1).decision;
        }

    }


    public enum Chance {
        OK,
        CANCEL,
        UNHANDLED
    }
}
