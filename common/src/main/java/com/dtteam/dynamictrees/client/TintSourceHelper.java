package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;

public class TintSourceHelper {

    public static int getFoliageColor(LeavesProperties leavesProperties, BlockAndTintGetter level, BlockState blockState, BlockPos pos) {
        BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(leavesProperties.getDynamicLeavesState(), 0);
        if (tintSource == null) return 0xFFFFFF;
        return tintSource.colorInWorld(blockState, level, pos);
    }

    public static int getLeavesColor(Species species, BlockAndLightGetter level, BlockPos pos) {
        LeavesProperties properties = species.getLeavesProperties();
        BlockState state = properties.getDynamicLeavesState();
        return level instanceof BlockAndTintGetter blockAndTintGetter ?
                getFoliageColor(properties, blockAndTintGetter, state, pos) :
                FoliageColor.FOLIAGE_DEFAULT;
    }

}
