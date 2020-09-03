package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

public interface ISeasonProvider {
	
	/**
	 * A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic seasons.
	 * A whole number is the beginning of a season with #.5 being the middle of the season.
	 * 
	 * 0 Spring
	 * 1 Summer
	 * 2 Fall
	 * 3 Winter
	 * 
	 * @return season value
	 */
	public float getSeasonValue(World world);
	
	/**
	 * A simple method for updating the handler every tick.
	 * 
	 * @param world
	 * @param worldTicks
	 */
	public void updateTick(World world, long worldTicks);
}