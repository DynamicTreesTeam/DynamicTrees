package com.dtteam.dynamictrees.api.worldgen;

import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.BiomeGenSettingsBuilderWrapper;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Provides the forest density for a given biome. Mods should implement these interfaces
 * to control how densely populated a {@link net.minecraft.world.level.biome.Biome} is.
 *
 * @author ferreusveritas
 */
public class BiomePropertySelectors {

    @FunctionalInterface
    public interface ChanceSelector {
        Chance getChance(RandomSource random, @NotNull Species species, int radius);
    }

    @FunctionalInterface
    public interface DensitySelector {
        double getDensity(RandomSource random, double noiseDensity);
    }

    @FunctionalInterface
    public interface SpeciesSelector {
        SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, RandomSource random);
    }

    public interface FeatureCancellation {

        /**
         * Removes features from the generation builder that match the cancellation criteria.
         */
        void cancelFeatures(BiomeGenSettingsBuilderWrapper generationSettingsBuilder);

        /**
         * Tells the canceller to cancel features using the given canceller.
         */
        void cancelUsing(FeatureCanceller featureCanceller);

        /**
         * Tells the canceller to cancel features whose names contain the given namespace.
         */
        void cancelWithNamespace(String namespace);

        /**
         * Tells the canceller to cancel features registered to the given step of feature generation.
         */
        void cancelDuring(GenerationStep.Decoration generationStep);

    }

    public static final class NoFeatureCancellation implements FeatureCancellation {

        public static final NoFeatureCancellation INSTANCE = new NoFeatureCancellation();

        private NoFeatureCancellation() {
        }

        @Override
        public void cancelFeatures(BiomeGenSettingsBuilderWrapper generationSettingsBuilder) {
        }

        @Override
        public void cancelUsing(FeatureCanceller featureCanceller) {
        }

        @Override
        public void cancelWithNamespace(String namespace) {
        }

        @Override
        public void cancelDuring(GenerationStep.Decoration generationStep) {
        }

    }

    public static final class NormalFeatureCancellation implements FeatureCancellation {

        private final Set<FeatureCanceller> cancellers;
        private final Set<String> namespaces;
        private final Collection<GenerationStep.Decoration> decorationSteps;

        public NormalFeatureCancellation() {
            this(Sets.newHashSet(), Sets.newHashSet(), Sets.newHashSet());
        }

        public NormalFeatureCancellation(Set<FeatureCanceller> cancellers, Set<String> namespaces, Set<GenerationStep.Decoration> decorationSteps) {
            this.cancellers = cancellers;
            this.namespaces = namespaces;
            this.decorationSteps = decorationSteps;
        }

        @Override
        public void cancelFeatures(BiomeGenSettingsBuilderWrapper generationSettingsBuilder) {
            decorationSteps.stream().map(generationSettingsBuilder::getFeatures).forEach(features ->
                    features.removeIf(placedFeatureHolder -> shouldRemovePlacedFeature(placedFeatureHolder.value()))
            );
        }

        private boolean shouldRemovePlacedFeature(PlacedFeature placedFeature) {
            return placedFeature.getFeatures().anyMatch(configuredFeature ->
                    cancellers.stream().anyMatch(canceller -> canceller.shouldCancel(configuredFeature, this))
            );
        }

        @Override
        public void cancelUsing(FeatureCanceller featureCanceller) {
            cancellers.add(featureCanceller);
        }

        @Override
        public void cancelWithNamespace(String namespace) {
            namespaces.add(namespace);
        }

        @Override
        public void cancelDuring(GenerationStep.Decoration generationStep) {
            decorationSteps.add(generationStep);
        }

        public void cancelDuringDefaultIfNoneSpecified() {
            if (decorationSteps.isEmpty()) {
                decorationSteps.add(GenerationStep.Decoration.VEGETAL_DECORATION);
            }
        }

        public void addFrom(NormalFeatureCancellation other) {
            this.cancellers.addAll(other.cancellers);
            this.namespaces.addAll(other.namespaces);
            this.decorationSteps.addAll(other.decorationSteps);
        }

        public void replaceFrom(NormalFeatureCancellation other) {
            this.cancellers.clear();
            this.namespaces.clear();
            this.decorationSteps.clear();
            this.addFrom(other);
        }
        public void reset() {
            this.cancellers.clear();
            this.namespaces.clear();
            this.decorationSteps.clear();
        }

        public Set<FeatureCanceller> getCancellers() {
            return cancellers;
        }

        public Set<String> getNamespaces() {
            return namespaces;
        }

        public Collection<GenerationStep.Decoration> getDecorationSteps() {
            return decorationSteps;
        }

        public boolean shouldCancelNamespace(String namespace) {
            return namespaces.contains(namespace);
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

        public SpeciesSelection(@NotNull Species species) {
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

        public StaticSpeciesSelector(@NotNull Species species) {
            this(new SpeciesSelection(species));
        }

        public StaticSpeciesSelector() {
            this(new SpeciesSelection());
        }

        @Override
        public SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, RandomSource random) {
            return decision;
        }
    }

    public static class RandomSpeciesSelector implements SpeciesSelector {

        private static class Entry {
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

        public RandomSpeciesSelector add(@NotNull Species species, int weight) {
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
        public SpeciesSelection getSpecies(BlockPos pos, BlockState dirt, RandomSource random) {
            int chance = random.nextInt(totalWeight);

            for (Entry entry : decisionTable) {
                if (chance < entry.weight) {
                    return entry.decision;
                }
                chance -= entry.weight;
            }

            return decisionTable.getLast().decision;
        }

    }


    public enum Chance {
        OK,
        CANCEL,
        UNHANDLED
    }
}
