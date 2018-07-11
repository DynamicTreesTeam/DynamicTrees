package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

/**
 * Season adapter for Tough As Nails mod
 * 
 * @author ferreusveritas
 *
 */
public class SeasonProviderTaN implements ISeasonProvider {
	
	@Override
	public float getSeasonValue() {
		return 0;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) {
	}
	
}
