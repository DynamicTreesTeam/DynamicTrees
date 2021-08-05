package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.seasons.*;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;

public class CompatHandler {

	public static void preInit() {
		if (Loader.isModLoaded(ModConstants.SERENESEASONS)) {
			HandleSereneSeasons();
		}
	}

	@Optional.Method(modid = ModConstants.SERENESEASONS)
	public static void HandleSereneSeasons() {
		SeasonManager seasonManager = new SeasonManager(
			world -> SeasonsConfig.isDimensionWhitelisted(world.provider.getDimension()) ?
				new Tuple<>(new SeasonProviderSereneSeasons(), new SeasonGrowthCalculatorActive()) :
				new Tuple<>(new SeasonProviderNull(), new SeasonGrowthCalculatorNull())
		);
		seasonManager.setTropicalPredicate((world, pos) -> BiomeConfig.usesTropicalSeasons(world.getBiome(pos)));
		SeasonHelper.setSeasonManager(seasonManager);
	}

}
