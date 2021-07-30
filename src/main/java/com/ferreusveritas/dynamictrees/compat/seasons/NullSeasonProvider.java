package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Season provider that does nothing at all
 * 
 * @author ferreusveritas
 */
public class NullSeasonProvider implements ISeasonProvider {
	
	public NullSeasonProvider() {}
	
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
