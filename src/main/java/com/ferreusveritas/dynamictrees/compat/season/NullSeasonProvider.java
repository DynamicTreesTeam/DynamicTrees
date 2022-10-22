package com.ferreusveritas.dynamictrees.compat.season;

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
    public Float getSeasonValue(Level level, BlockPos pos) {
        return null;
    }

    @Override
    public void updateTick(Level level, long dayTime) {
    }

    @Override
    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return false;
    }

}
