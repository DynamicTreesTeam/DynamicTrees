package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Season provider that does nothing at all
 *
 * @author ferreusveritas
 */
public class NullSeasonProvider implements SeasonProvider {

    public NullSeasonProvider() {
    }

    @Override
    public Float getSeasonValue(Level world, BlockPos pos) {
        return null;
    }

    @Override
    public void updateTick(Level world, long worldTicks) {
    }

    @Override
    public boolean shouldSnowMelt(Level world, BlockPos pos) {
        return false;
    }

}
