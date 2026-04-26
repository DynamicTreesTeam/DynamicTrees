package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PottedSaplingTintSource extends SaplingTintSource {

    public PottedSaplingTintSource(BlockColors colors) {
        super(colors, Species.NULL_SPECIES);
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        species = DTRegistries.POTTED_SAPLING.get().getSpecies(level, pos);
        return super.colorInWorld(state,level,pos);
    }
}
