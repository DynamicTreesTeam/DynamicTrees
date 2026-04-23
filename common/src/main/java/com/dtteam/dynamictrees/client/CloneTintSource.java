package com.dtteam.dynamictrees.client;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class CloneTintSource implements BlockTintSource {
    final static int magenta = 0x00FF00FF;//for errors... because magenta sucks.

    private Supplier<BlockTintSource> original;

    public CloneTintSource (Supplier<BlockTintSource> original){
        this.original = original;
    }

    @Override
    public int color(@NonNull BlockState blockState) {
        BlockTintSource source = original.get();
        return source == null ? magenta : source.color(blockState);
    }
    @Override
    public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
        BlockTintSource source = original.get();
        return source == null ? magenta : source.colorInWorld(state, level, pos);
    }
}