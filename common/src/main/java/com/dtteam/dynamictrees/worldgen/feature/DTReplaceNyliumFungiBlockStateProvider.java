package com.dtteam.dynamictrees.worldgen.feature;

import net.minecraft.world.level.WorldGenLevel;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class DTReplaceNyliumFungiBlockStateProvider extends BlockStateProvider {
    public static final MapCodec<DTReplaceNyliumFungiBlockStateProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("enabled").forGetter(provider -> provider.enabled),
            BlockStateProvider.CODEC.fieldOf("disabled").forGetter(provider -> provider.disabled)
    ).apply(instance, DTReplaceNyliumFungiBlockStateProvider::new));
    public final BlockStateProvider enabled;
    public final BlockStateProvider disabled;

    public DTReplaceNyliumFungiBlockStateProvider(BlockStateProvider enabled, BlockStateProvider disabled) {
        this.enabled = enabled;
        this.disabled = disabled;
    }

    protected BlockStateProviderType<?> type() {
        return DTRegistries.REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE.get();
    }

    public BlockState getState(WorldGenLevel genLevel, RandomSource random, BlockPos state) {
        return DTConfigs.COMMON.replaceNyliumFungi.get()
                ? this.enabled.getState(genLevel, random, state)
                : this.disabled.getState(genLevel, random, state);
    }
}
