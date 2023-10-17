package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.worldgen.CaveRootedTreePlacement;
import com.ferreusveritas.dynamictrees.worldgen.DTFeatures;
import com.ferreusveritas.dynamictrees.worldgen.feature.DTReplaceNyliumFungiBlockStateProvider;
import com.ferreusveritas.dynamictrees.worldgen.structure.VillageTreeReplacement;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DataPackRegistriesHooks;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DTDatapackBuiltinEntriesProvider extends DatapackBuiltinEntriesProvider implements DTDataProvider {
    public DTDatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, Set<String> modIds) {
        super(output, registries.thenApply(p -> constructRegistries(p, getBuilder(p))), modIds);
    }

    @SuppressWarnings("unchecked")
    private static HolderLookup.Provider constructRegistries(HolderLookup.Provider original, RegistrySetBuilder datapackEntriesBuilder) {
        try {
            // We don't need SRG mappings; this is for in-dev datagen only
            Field ownerField = ObfuscationReflectionHelper.findField(Holder.Reference.class, "owner");
            Object holderOwner = ownerField.get(original.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.BADLANDS));
            Class<?> compositeOwnerClass = Class.forName("net.minecraft.core.RegistrySetBuilder$CompositeOwner");
            Field ownersField = ObfuscationReflectionHelper.findField(compositeOwnerClass, "owners");
            Set<HolderOwner<?>> owners = (Set<HolderOwner<?>>) ownersField.get(holderOwner);
            var builderKeys = new HashSet<>(datapackEntriesBuilder.getEntryKeys());
            DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().filter(data -> !builderKeys.contains(data.key())).forEach(data -> datapackEntriesBuilder.add(data.key(), context -> {}));
            HolderLookup.Provider provider = datapackEntriesBuilder.buildPatch(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), original);
            Object newHolderOwner = ownerField.get(provider.lookupOrThrow(Registries.CONFIGURED_FEATURE).getOrThrow(DTFeatures.DYNAMIC_TREE_CONFIGURED_FEATURE));
            owners.addAll((Set<HolderOwner<?>>) ownersField.get(newHolderOwner));
            return provider;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RegistrySetBuilder getBuilder(HolderLookup.Provider vanillaProvider) {
        return new RegistrySetBuilder()
                .add(Registries.TEMPLATE_POOL, context -> bootstrapTemplatePools(vanillaProvider, context))
                .add(Registries.CONFIGURED_FEATURE, context -> bootstrapConfiguredFeatures(vanillaProvider, context))
                .add(Registries.PLACED_FEATURE, DTDatapackBuiltinEntriesProvider::bootstrapPlacedFeatures);
    }

    private static void bootstrapTemplatePools(HolderLookup.Provider vanillaProvider, BootstapContext<StructureTemplatePool> context) {
        // TODO 1.20: Verify this works
        VillageTreeReplacement.replaceTreesFromVanillaVillages(vanillaProvider, context);
    }

    private static void bootstrapConfiguredFeatures(HolderLookup.Provider vanillaProvider, BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(DTFeatures.DYNAMIC_TREE_CONFIGURED_FEATURE,
                new ConfiguredFeature<>(DTRegistries.DYNAMIC_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(DTFeatures.CAVE_ROOTED_TREE_CONFIGURED_FEATURE,
                new ConfiguredFeature<>(DTRegistries.CAVE_ROOTED_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

        // TODO 1.20: Verify this works
        replaceNyliumFungiFeatures(vanillaProvider, context);
    }

    private static void bootstrapPlacedFeatures(BootstapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(DTFeatures.DYNAMIC_TREE_PLACED_FEATURE,
                new PlacedFeature(configuredFeatures.getOrThrow(DTFeatures.DYNAMIC_TREE_CONFIGURED_FEATURE), List.of()));
        context.register(DTFeatures.CAVE_ROOTED_TREE_PLACED_FEATURE,
                new PlacedFeature(configuredFeatures.getOrThrow(DTFeatures.CAVE_ROOTED_TREE_CONFIGURED_FEATURE), List.of(
                        CaveRootedTreePlacement.INSTANCE, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
                        EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome())));
    }

    private static void replaceNyliumFungiFeatures(HolderLookup.Provider vanillaProvider, BootstapContext<ConfiguredFeature<?, ?>> context) {
        TreeRegistry.findSpecies(DTTrees.CRIMSON).getSapling().ifPresent(crimsonSapling ->
                TreeRegistry.findSpecies(DTTrees.WARPED).getSapling().ifPresent(warpedSapling -> {
                    var configuredFeatures = vanillaProvider.lookup(Registries.CONFIGURED_FEATURE).orElseThrow();
                    List.of(NetherFeatures.CRIMSON_FOREST_VEGETATION, NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL,
                                    NetherFeatures.WARPED_FOREST_VEGETION, NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL)
                            .forEach(key -> replaceFeature(context, configuredFeatures, key, crimsonSapling, warpedSapling));
                })
        );
    }

    private static void replaceFeature(BootstapContext<ConfiguredFeature<?, ?>> context, HolderLookup.RegistryLookup<ConfiguredFeature<?, ?>> configuredFeatures,
            ResourceKey<ConfiguredFeature<?, ?>> key, Block crimsonSapling, Block warpedSapling) {
        var feature = configuredFeatures.getOrThrow(key).value();
        var config = (NetherForestVegetationConfig) feature.config();
        var stateProvider = (WeightedStateProvider) config.stateProvider;

        var newConfig = new NetherForestVegetationConfig(replaceBlockStates(stateProvider, crimsonSapling, warpedSapling), config.spreadWidth, config.spreadHeight);
        context.register(key, new ConfiguredFeature<>(Feature.NETHER_FOREST_VEGETATION, newConfig));
    }

    private static BlockStateProvider replaceBlockStates(WeightedStateProvider stateProvider, Block crimsonSapling, Block warpedSapling) {
        var listBuilder = SimpleWeightedRandomList.<BlockState>builder();

        for (var entry : stateProvider.weightedList.unwrap()) {
            BlockState blockState = entry.getData();
            if (blockState.is(Blocks.CRIMSON_FUNGUS)) {
                blockState = crimsonSapling.defaultBlockState();
            } else if (blockState.is(Blocks.WARPED_FUNGUS)) {
                blockState = warpedSapling.defaultBlockState();
            }

            listBuilder.add(blockState, entry.getWeight().asInt());
        }

        return new DTReplaceNyliumFungiBlockStateProvider(new WeightedStateProvider(listBuilder), stateProvider);
    }
}
