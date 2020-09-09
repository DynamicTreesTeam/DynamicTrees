package com.ferreusveritas.dynamictrees.seasons;

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
	public Float getSeasonValue(World world) {
		return null;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) { }

}
