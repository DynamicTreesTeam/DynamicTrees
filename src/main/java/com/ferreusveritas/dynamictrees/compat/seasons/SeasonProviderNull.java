package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Season provider that does nothing at all
 * 
 * @author ferreusveritas
 *
 */
public class SeasonProviderNull implements ISeasonProvider {
	
	
	public SeasonProviderNull() {}
	
	@Override
	public Float getSeasonValue(World world, BlockPos pos) {
		return null;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) { }
	
	@Override
	public boolean shouldSnowMelt(World world, BlockPos pos) {
		return false;
	}
	
}
