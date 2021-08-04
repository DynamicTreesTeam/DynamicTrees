package com.ferreusveritas.dynamictrees.compat.seasons;

import corgitaco.betterweather.api.season.Season;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class BetterWeatherSeasonProvider implements ISeasonProvider {

    private Float seasonValue = 1.0f;

    @Override
    public Float getSeasonValue(World world, BlockPos pos) {
        return seasonValue;
    }

    @Override
    public void updateTick(World world, long worldTicks) {
        final Season season = Season.getSeason(world);
        seasonValue = season == null ? null :
                (season.getKey().ordinal() * 3 + season.getPhase().ordinal() + 0.5F) / 3F;
    }

    @Override
    public boolean shouldSnowMelt(World world, BlockPos pos) {
        // BW uses a mixin on getBaseTemperature().
        return world.getBiome(pos).getBaseTemperature() >= 0.15f;
    }

}
