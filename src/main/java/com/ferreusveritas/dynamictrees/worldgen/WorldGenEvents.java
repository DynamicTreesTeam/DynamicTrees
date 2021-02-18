package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.events.TreeCancelRegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.worldgen.canceller.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class WorldGenEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void addDynamicTrees (final BiomeLoadingEvent event) {
        if (!WorldGenRegistry.isWorldGenEnabled())
            return;

        event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_FEATURE.withConfiguration(new NoFeatureConfig()));
    }

    /**
     * This is not an ideal way of removing trees, but it's the best way I've currently found.
     * It currently loops through the vegetal features of the current biome and removes them if they are found to contain trees.
     *
     * @param event The biome loading event.
     */
    @SubscribeEvent
    public void removeVanillaTrees(final BiomeLoadingEvent event) {
        // TODO: Currently, any mods that don't create their own Feature for trees will have it removed (if they use a ConfiguredFeature that uses Feature.TREE).
        if (!WorldGenRegistry.isWorldGenEnabled())
            return;

        final ResourceLocation biomeResLoc = event.getName();

        if (biomeResLoc == null) return;

        // Gets the Json tree canceller.
        ITreeCanceller treeCanceller = WorldGenRegistry.getJsonTreeCanceller();

        if (!treeCanceller.shouldCancelFeatures(biomeResLoc))
            return;

        // Ensure tree feature cancellers have been registered.
        TreeFeatureCancellerRegistry.registerCancellers();

        // Loop through all vegetal features and remove if found to contain trees.
        event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION).removeIf(configuredFeatureSupplier -> {
            // Go through each canceller for the current biome and remove the current feature if it shouldCancel returns true.
            for (ITreeFeatureCanceller canceller : treeCanceller.getFeatureCancellers(biomeResLoc)) {
                if (canceller != null && canceller.shouldCancel(configuredFeatureSupplier.get(), biomeResLoc, treeCanceller))
                    return true;
            }
            return false;
        });
    }

    ///////////////////////////////////////////
    // Registries
    ///////////////////////////////////////////

    @SubscribeEvent
    public void onTreeCancelRegistry(TreeCancelRegistryEvent event) {
        // Since removeVanillaTrees will be called on every biome load (so for every world load) and is checking every feature,
        // we should register the cancellers only for the biomes that need it to keep loading times as low as possible.

        final ITreeCanceller treeCanceller = event.getTreeCanceller();
        final List<String> namespaces = Collections.singletonList(DynamicTrees.MINECRAFT_ID);

        // Gets a list of all vanilla Minecraft biome registry keys.
        final List<RegistryKey<Biome>> vanillaBiomes = ForgeRegistries.BIOMES.getEntries().stream().map(Map.Entry::getKey)
                .filter(key -> key.getLocation().getNamespace().equals(DynamicTrees.MINECRAFT_ID)).collect(Collectors.toList());

        // This registers the cancellation of all tree features with the namespace "minecraft" from all overworld biomes with the namespace "minecraft".
        vanillaBiomes.stream().filter(key -> BiomeDictionary.hasType(key, BiomeDictionary.Type.OVERWORLD)).forEach(key ->
                treeCanceller.register(key.getLocation(), namespaces, Collections.singletonList(TreeFeatureCancellerRegistry.TREE_CANCELLER)));

        // This registers the cancellation of giant fungus features with the namespace "minecraft" from the warped and crimson forest biomes.
        Stream.of(Biomes.WARPED_FOREST, Biomes.CRIMSON_FOREST).map(RegistryKey::getLocation).forEach(biomeResLoc ->
            treeCanceller.register(biomeResLoc, namespaces, Collections.singletonList(TreeFeatureCancellerRegistry.FUNGUS_CANCELLER))
        );

        // This registers the cancellation of huge mushroom features with the namespace "minecraft" from the mushroom island biomes.
        Stream.of(Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE).map(RegistryKey::getLocation).forEach(biomeResLoc ->
                treeCanceller.register(biomeResLoc, namespaces, Collections.singletonList(TreeFeatureCancellerRegistry.MUSHROOM_CANCELLER))
        );
    }

    @SubscribeEvent
    public void onTreeFeatureCancelRegistry(TreeFeatureCancellerRegistry.TreeFeatureCancellerRegistryEvent event) {
        final TreeFeatureCancellerRegistry registry = event.getFeatureCancellerRegistry();

        // This registers default tree feature canceller, which will cancel any features if their config extends BaseTreeFeatureConfig.
        registry.register(TreeFeatureCancellerRegistry.TREE_CANCELLER, new TreeFeatureCanceller<>(BaseTreeFeatureConfig.class));

        // This registers the tree feature canceller for fungus, which will cancel any features if their config extends HugeFungusConfig.
        registry.register(TreeFeatureCancellerRegistry.FUNGUS_CANCELLER, new FungusFeatureCanceller<>(HugeFungusConfig.class));

        // This registers the tree feature canceller for mushrooms, which will cancel any features if their config extends BigMushroomFeatureConfig.
        registry.register(TreeFeatureCancellerRegistry.MUSHROOM_CANCELLER, new MushroomFeatureCanceller<>(BigMushroomFeatureConfig.class));
    }

}
