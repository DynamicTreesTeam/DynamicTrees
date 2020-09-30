package com.ferreusveritas.dynamictrees.seasons;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonManager;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonHelper {

	// A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic seasons.
	// A whole number is the beginning of a season with #.5 being the middle of the season.
	public static final float SPRING = 0.0f;
	public static final float SUMMER = 1.0f;
	public static final float AUTUMN = 2.0f;
	public static final float WINTER = 3.0f;
	
	// Tropical convenience values.
	public static final float DRY = SUMMER;
	public static final float WET = WINTER;
	
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
		return globalSeasonalGrowthFactor(world, rootPos, 0);
	}
	static public float globalSeasonalGrowthFactor(World world, BlockPos rootPos, float offset) {
		return seasonManager.getGrowthFactor(world, rootPos, offset);
	}
	
	static public float globalSeasonalSeedDropFactor(World world, BlockPos pos) {
		return globalSeasonalSeedDropFactor(world, pos, 0);
	}
	static public float globalSeasonalSeedDropFactor(World world, BlockPos pos, float offset) {
		return seasonManager.getSeedDropFactor(world, pos, offset);
	}
	
	static public float globalSeasonalFruitProductionFactor(World world, BlockPos pos) {
		return globalSeasonalFruitProductionFactor(world, pos, 0);
	}
	static public float globalSeasonalFruitProductionFactor(World world, BlockPos pos, float offset) {
		return seasonManager.getFruitProductionFactor(world, pos, offset);
	}
	
	/**
	 * @param world The world
	 * @return season value 0.0(Early Spring, Inclusive) -> 4.0(Later Winter, Exclusive) or null if there's no seasons in the world.
	 */
	static public Float getSeasonValue(World world) {
		return seasonManager.getSeasonValue(world);
	}
	
	/**
	 * Tests if the position in world is considered tropical and
	 * thus follows tropical season rules.
	 * 
	 * @param world
	 * @param pos
	 * @return
	 */
	static public boolean isTropical(World world, BlockPos pos) {
		return seasonManager.isTropical(world, pos);
	}
	
	/**
	 * Test if the season value falls between two seasonal points.
	 * Wraps around the Spring/Winter point(0) if seasonA > seasonB;
	 * 
	 * @param testValue value to test
	 * @param SeasonA chronological season boundary beginning
	 * @param SeasonB chronological season boundary ending
	 * @return
	 */
	static public boolean isSeasonBetween(float testValue, float SeasonA, float SeasonB) {
		
		testValue %= 4.0f;
		SeasonA %= 4.0f;
		SeasonB %= 4.0f;
		
		if(SeasonA <= SeasonB) {
			return testValue > SeasonA && testValue < SeasonB; //Simply between point A and B(inside)
		} else {
			return testValue < SeasonB || testValue > SeasonA; //The test wraps around the zero point(outside)
		}
		
	}
	
}
