package com.dtteam.dynamictrees.client.tint;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vanilla {@link net.minecraft.client.color.block.BlockColors} lookup used by falling-leaf particles and mods
 * that tint fractures from {@code getTintSource(state, index)}.
 */
public record FunctionalBlockTintSource(int fallback, WorldColor color) implements BlockTintSource {

    @FunctionalInterface
    public interface WorldColor {
        int color(BlockState state, BlockAndTintGetter level, BlockPos pos);
    }

    @Override
    public int color(BlockState state) {
        return fallback;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return fallback;
        }
        return color.color(state, level, pos);
    }
}
