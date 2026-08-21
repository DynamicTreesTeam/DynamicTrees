package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.api.season.SeasonProvider;
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

    public Float getSeasonValue(Level level, BlockPos pos) {
        return null;
    }

    public void updateTick(Level level, long dayTime) {
    }

    public boolean shouldSnowMelt(Level level, BlockPos pos) {
        return false;
    }

}
