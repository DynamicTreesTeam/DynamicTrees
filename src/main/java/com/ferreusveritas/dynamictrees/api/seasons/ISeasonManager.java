package com.ferreusveritas.dynamictrees.api.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Manages Seasonal output rates
 *  
 * @author ferreusveritas
 */
public interface ISeasonManager {
	
	public void updateTick(World world, long worldTicks);
	
	public float getGrowthRate(World world, BlockPos rootPos);
	
	public float getSeedDropRate(World world, BlockPos rootPos);

	public float getSeasonValue(World world);
	
}
