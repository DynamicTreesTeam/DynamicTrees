package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

/**
 * Season adapter for TerraFirmaCraft
 * 
 * @author ferreusveritas
 *
 */
public class SeasonProviderTFC implements ISeasonProvider {
	
	@Override
	public float getSeasonValue() {
		return 0;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) {
	}
	
}
