package com.ferreusveritas.dynamictrees.seasons;

import net.minecraft.world.World;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;

public class SeasonProviderSereneSeasons implements ISeasonProvider {

	private float seasonValue = 1.0f;
	
	@Override
	public float getSeasonValue(World world) {
		return seasonValue;
	}

	@Override
	public void updateTick(World world, long worldTicks) {
		seasonValue = ((SeasonHelper.getSeasonState(world).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
	}

}
