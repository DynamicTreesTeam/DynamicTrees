package com.ferreusveritas.dynamictrees.worldgen.feature;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class DTReplaceNyliumFungiBlockStateProvider extends BlockStateProvider {
    public static final Codec<DTReplaceNyliumFungiBlockStateProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("enabled").forGetter(provider -> provider.enabled),
            BlockStateProvider.CODEC.fieldOf("disabled").forGetter(provider -> provider.disabled)
    ).apply(instance, DTReplaceNyliumFungiBlockStateProvider::new));
    public final BlockStateProvider enabled;
    public final BlockStateProvider disabled;

    public DTReplaceNyliumFungiBlockStateProvider(BlockStateProvider enabled, BlockStateProvider disabled) {
        this.enabled = enabled;
        this.disabled = disabled;
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return DTRegistries.REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE.get();
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos state) {
        return DTConfigs.REPLACE_NYLIUM_FUNGI.get()
                ? this.enabled.getState(random, state)
                : this.disabled.getState(random, state);
    }
}
