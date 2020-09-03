package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.seasons.SeasonManager;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager.SeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.seasons.SeasonProviderSereneSeasons;

import net.minecraft.util.Tuple;

public class SereneSeasonsAdapter implements ICompatAdapter {

	@Override
	public void Init() {
		SeasonManager.setSeasonMapper(world -> new Tuple(new SeasonProviderSereneSeasons(), new SeasonGrowthCalculator()));
	}
	
}
