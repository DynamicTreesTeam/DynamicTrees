package com.ferreusveritas.dynamictrees.compat.seasons;

import corgitaco.betterweather.api.season.Season;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public class BetterWeatherSeasonProvider implements SeasonProvider {

    private Float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(Level world, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(Level world, long worldTicks) {
        final Season season = Season.getSeason(world);
        seasonValue = season == null ? null :
                (season.getKey().ordinal() * 3 + season.getPhase().ordinal() + 0.5F) / 3F;
    }

    @Override
    public boolean shouldSnowMelt(Level world, BlockPos pos) {
        // BW uses a mixin on getBaseTemperature().
        return world.getBiome(pos).getBaseTemperature() >= 0.15f;
    }

}
