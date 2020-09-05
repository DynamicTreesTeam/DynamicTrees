package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.seasons.ISeasonProvider;
import com.ferreusveritas.dynamictrees.seasons.SeasonGrowthCalculatorActive;
import com.ferreusveritas.dynamictrees.seasons.SeasonGrowthCalculatorNull;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.seasons.SeasonManager;
import com.ferreusveritas.dynamictrees.seasons.SeasonProviderNull;
import com.ferreusveritas.dynamictrees.seasons.SeasonProviderSereneSeasons;

import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import sereneseasons.config.SeasonsConfig;

public class SereneSeasonsAdapter implements ICompatAdapter {
	
	@Override
	public void Init() {
		SeasonHelper.setSeasonManager( new SeasonManager(world -> mapper(world)));
	}
	
	public Tuple<ISeasonProvider, ISeasonGrowthCalculator>  mapper(World world) {
		
		int dim = world.provider.getDimension();
		if(SeasonsConfig.isDimensionWhitelisted(dim)) {
			return new Tuple(new SeasonProviderSereneSeasons(), new SeasonGrowthCalculatorActive());
		}
		
		return new Tuple(new SeasonProviderNull(), new SeasonGrowthCalculatorNull());
	}
	
}
