package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

public class SeasonProviderNull implements ISeasonProvider {

	protected float lockedSeasonValue;
	
	public SeasonProviderNull() {
		this(1.0f);
	}
	
	public SeasonProviderNull(float seasonValue) {
		lockedSeasonValue = seasonValue;
	}
	
	@Override
	public float getSeasonValue() {
		return lockedSeasonValue;
	}

	@Override
	public void updateTick(World world, long worldTicks) {
	}

}
