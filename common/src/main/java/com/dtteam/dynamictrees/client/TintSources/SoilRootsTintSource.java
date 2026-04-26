package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.block.soil.SoilBlock;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SoilRootsTintSource implements BlockTintSource {

    SoilBlock soilBlock;

    public SoilRootsTintSource (SoilBlock soilProperties){
        this.soilBlock = soilProperties;
    }

    @Override
    public int color(BlockState blockState) {
        return -1;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return soilBlock.rootColor(state, level, pos);
    }
}
