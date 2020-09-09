package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonManager;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonHelper {

	private static ISeasonManager seasonManager = new SeasonManager();

	static public ISeasonManager getSeasonManager() {
		return seasonManager;
	}
	
	/** Maybe you don't like the global function season function.  Fine, do it all yourself then! */
	static public void setSeasonManager(ISeasonManager manager) {
		seasonManager = manager;
	}
	
	static public void updateTick(World world, long worldTicks) {
		seasonManager.updateTick(world, worldTicks);
	}
	
	static public float globalSeasonalGrowthFactor(World world, BlockPos rootPos) {
		return seasonManager.getGrowthFactor(world, rootPos);
	}
	
	static public float globalSeasonalSeedDropFactor(World world, BlockPos pos) {
		return seasonManager.getSeedDropFactor(world, pos);
	}
	
	static public float globalSeasonalFruitProductionFactor(World world, BlockPos pos) {
		return seasonManager.getFruitProductionFactor(world, pos);
	}
	
	/**
	 * @param world The world
	 * @return season value 0.0(Early Spring, Inclusive) -> 4.0(Later Winter, Exclusive) or null if there's no seasons in the world.
	 */
	static public Float getSeasonValue(World world) {
		return seasonManager.getSeasonValue(world);
	}
	
}
