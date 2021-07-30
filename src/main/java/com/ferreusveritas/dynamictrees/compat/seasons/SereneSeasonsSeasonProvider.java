package com.ferreusveritas.dynamictrees.compat.seasons;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import sereneseasons.api.season.Season.SubSeason;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;
import sereneseasons.season.SeasonHooks;

import java.util.Objects;

public class SereneSeasonsSeasonProvider implements ISeasonProvider {
	
	private float seasonValue = 1.0f;
	
	@Override
	public Float getSeasonValue(World world, BlockPos pos) {
		return seasonValue;
	}
	
	@Override
	public void updateTick(World world, long worldTicks) {
		seasonValue = ((SeasonHelper.getSeasonState(world).getSubSeason().ordinal() + 0.5f) / SubSeason.VALUES.length) * 4.0f;
	}
	
	@Override
	public boolean shouldSnowMelt(World world, BlockPos pos) {
		if (SeasonsConfig.generateSnowAndIce.get() && seasonValue < com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper.WINTER) {
			final RegistryKey<Biome> biome = RegistryKey.create(Registry.BIOME_REGISTRY, Objects.requireNonNull(world.getBiome(pos).getRegistryName()));
			return BiomeConfig.enablesSeasonalEffects(biome) &&
					SeasonHooks.getBiomeTemperature(world, biome, pos) >= 0.15f;
		}
		return false;
	}
	
}
