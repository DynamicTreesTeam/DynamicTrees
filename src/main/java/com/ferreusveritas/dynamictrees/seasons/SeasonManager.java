package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;

public class SeasonManager {

	//static protected ISeasonProvider seasonProvider = new SeasonProviderBasic();
	static protected ISeasonProvider seasonProvider = new SeasonProviderNull();
	
	public void updateTick(World world, long worldTicks) {
		seasonProvider.updateTick(world, worldTicks);
	}
	
	public float getSeasonValue() {
		return seasonProvider.getSeasonValue();
	}
	
	static public void setSeasonProvider(ISeasonProvider provider) {
		seasonProvider = provider;
	}
}
