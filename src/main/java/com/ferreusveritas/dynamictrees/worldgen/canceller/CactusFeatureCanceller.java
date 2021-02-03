package com.ferreusveritas.dynamictrees.worldgen.canceller;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.block.CactusBlock;

/**
 * This class cancels any features that have a config that extends {@link BlockClusterFeatureConfig} and that
 * has a block set within that class that extends the cactusBlockClass given (by default {@link CactusBlock}).
 *
 * @author Harley O'Connor
 */
public class CactusFeatureCanceller<T extends Block> implements ITreeFeatureCanceller {

    private final Class<T> cactusBlockClass;

    public CactusFeatureCanceller(Class<T> cactusBlockClass) {
        this.cactusBlockClass = cactusBlockClass;
    }

    @Override
    public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, ResourceLocation biomeResLoc, ITreeCanceller treeCanceller) {
        IFeatureConfig featureConfig = configuredFeature.config;

        if (!(featureConfig instanceof DecoratedFeatureConfig))
            return false;

        featureConfig = ((DecoratedFeatureConfig) featureConfig).feature.get().config;

        if (!(featureConfig instanceof DecoratedFeatureConfig))
            return false;

        ConfiguredFeature<?, ?> currentConfiguredFeature = ((DecoratedFeatureConfig) featureConfig).feature.get();
        ResourceLocation featureResLoc = currentConfiguredFeature.feature.getRegistryName();
        featureConfig = currentConfiguredFeature.config;

        if (!(featureConfig instanceof BlockClusterFeatureConfig))
            return false;

        BlockClusterFeatureConfig blockClusterFeatureConfig = ((BlockClusterFeatureConfig) featureConfig);
        BlockStateProvider stateProvider = blockClusterFeatureConfig.stateProvider;

        if (!(stateProvider instanceof SimpleBlockStateProvider))
            return false;

        // SimpleBlockStateProvider does not use random or BlockPos in getBlockState, so giving null is safe.
        return this.cactusBlockClass.isInstance(stateProvider.getBlockState(null, null).getBlock())
                && treeCanceller.shouldCancelFeature(biomeResLoc, featureResLoc);
    }

}
