package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface SeasonProvider {

    /**
     * A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic
     * seasons. A whole number is the beginning of a season with #.5 being the middle of the season.
     * <p>
     * 0 Spring 1 Summer 2 Autumn 3 Winter
     *
     * @return season value as a Float object or null if seasons are not enabled
     */
    Float getSeasonValue(Level world, BlockPos pos);

    /**
     * A simple method for updating the handler every tick.
     *
     * @param world
     * @param worldTicks
     */
    void updateTick(Level world, long worldTicks);

    boolean shouldSnowMelt(Level world, BlockPos pos);

}