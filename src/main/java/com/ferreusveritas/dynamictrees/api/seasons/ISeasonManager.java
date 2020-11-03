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
	
	public void flushMappings();
	
	public float getGrowthFactor(World world, BlockPos rootPos, float offset);
	
	public float getSeedDropFactor(World world, BlockPos rootPos, float offset);
	
	public float getFruitProductionFactor(World world, BlockPos rootPos, float offset);
	
	public Float getSeasonValue(World world, BlockPos rootPos);
	
	public boolean isTropical(World world, BlockPos rootPos);
	
	public boolean shouldSnowMelt(World world, BlockPos pos);
	
}
