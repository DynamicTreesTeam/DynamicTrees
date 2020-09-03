package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.seasons.ActiveSeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager;
import com.ferreusveritas.dynamictrees.seasons.SeasonProviderSereneSeasons;

import net.minecraft.util.Tuple;

public class SereneSeasonsAdapter implements ICompatAdapter {

	@Override
	public void Init() {
		SeasonHelper.setSeasonManager( new SeasonManager(world -> new Tuple(new SeasonProviderSereneSeasons(), new ActiveSeasonGrowthCalculator())) );
	}
	
}
