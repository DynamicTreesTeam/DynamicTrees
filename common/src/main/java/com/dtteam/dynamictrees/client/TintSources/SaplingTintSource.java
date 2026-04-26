package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SaplingTintSource implements BlockTintSource {

    protected final BlockColors colors;
    protected Species species;

    public SaplingTintSource(BlockColors colors, Species species){
        this.species = species;
        this.colors = colors;
    }

    @Override
    public int color(BlockState blockState) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (!species.shouldTintSapling()) return -1;
        LeavesProperties leaves = species.getLeavesProperties();
        if (leaves.getDynamicLeavesBlock().isPresent()){
            BlockTintSource source = colors.getTintSource(leaves.getDynamicLeavesState(), 0);
            if (source != null) return source.colorInWorld(leaves.getDynamicLeavesState(), level, pos);
        }
        return -1;
    }
}
