package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class CloneTintSource implements BlockTintSource {
    final static int magenta = 0x00FF00FF;//for errors... because magenta sucks.

    private final Supplier<BlockTintSource> originalSup;
    private BlockTintSource original = null;
    private BlockTintSource getOriginal(){
        if (original == null) {
            original = originalSup.get();
            if (original == null) original = BlockColorMultipliers.BLANK_LAYER;
        }
        return original;
    }

    public CloneTintSource (Supplier<BlockTintSource> original){
        this.originalSup = original;
    }

    @Override
    public int color(@NonNull BlockState blockState) {
        BlockTintSource source = getOriginal();
        return source == null ? magenta : source.color(blockState);
    }
    @Override
    public int colorInWorld(@NonNull BlockState state, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos) {
        BlockTintSource source = getOriginal();
        return source == null ? magenta : source.colorInWorld(state, level, pos);
    }

    public static List<BlockTintSource> cloneAllSources(BlockColors colors, Supplier<BlockState> state, int count){
        List<BlockTintSource> sources = new LinkedList<>();
        for (int i=0; i<count; i++){
            final int layer = i;
            sources.add(new CloneTintSource(()->colors.getTintSource(state.get(), layer)));
        }
        return sources;
    }

}