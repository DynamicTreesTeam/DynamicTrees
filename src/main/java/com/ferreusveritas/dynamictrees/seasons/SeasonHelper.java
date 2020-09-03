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
	
	static public float globalSeasonalGrowthRate(World world, BlockPos rootPos) {
		return seasonManager.getGrowthRate(world, rootPos);
	}
	
	static public float globalSeasonalSeedDropRate(World world, BlockPos pos) {
		return seasonManager.getSeedDropRate(world, pos);
	}
	
	static public float getSeasonValue(World world) {
		return seasonManager.getSeasonValue(world);
	}
	
}
