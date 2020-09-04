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
	
	public float getGrowthFactor(World world, BlockPos rootPos);
	
	public float getSeedDropFactor(World world, BlockPos rootPos);

	public float getFruitProductionFactor(World world, BlockPos rootPos);
	
	public float getSeasonValue(World world);
	
}
