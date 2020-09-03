package com.ferreusveritas.dynamictrees.seasons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SeasonManager {

	////////////////////////////////////////////////////////////////
	// Season Growth Calculator
	////////////////////////////////////////////////////////////////
	
	/***
	 * Provides a means to calculate temperate and tropical growth rate values
	 * 
	 * @author ferreusveritas
	 */
	public static class SeasonGrowthCalculator {
	
		float calcTemperateGrowthRate(float seasonValue) {
			return MathHelper.clamp(((((float)Math.sin(seasonValue / 2 * Math.PI) + 1) / 2f) * 1.75f) - 0.5f, 0.0f, 1.0f);
		}
		
		float calcTropicalGrowthRate(float seasonValue) {
			return 1.0f;//TODO
		}
		
	}
		
	
	////////////////////////////////////////////////////////////////
	// Season Context
	////////////////////////////////////////////////////////////////
	
	private static class SeasonContext {
		private ISeasonProvider provider;
		private SeasonGrowthCalculator calculator;
		private float temperateValue;
		private float tropicalValue;
		
		public SeasonContext(ISeasonProvider provider, SeasonGrowthCalculator calculator) {
			this.provider = provider;
			this.calculator = calculator;
		}
		
		public void updateTick(World world, long worldTicks) {
			if(worldTicks % 20 == 0) {
				provider.updateTick(world, worldTicks);
				float seasonValue = provider.getSeasonValue(world);
				temperateValue = calculator.calcTemperateGrowthRate(seasonValue);
				tropicalValue = calculator.calcTropicalGrowthRate(seasonValue);
			}
		}
		
		public ISeasonProvider getSeasonProvider() {
			return provider;
		}
		
		public float getTemperateValue() {
			return temperateValue;
		}
		
		public float getTropicalValue() {
			return tropicalValue;
		}
		
	}
	
	static private Map<Integer, SeasonContext> seasonContextMap = new HashMap<>();
	
	
	////////////////////////////////////////////////////////////////
	// Season Mapper
	////////////////////////////////////////////////////////////////
	
	static private Function<World, Tuple<ISeasonProvider, SeasonGrowthCalculator> > seasonMapper = w -> new Tuple(new SeasonProviderNull(), new SeasonGrowthCalculator());
	
	static public void setSeasonMapper(Function<World, Tuple<ISeasonProvider, SeasonGrowthCalculator> > seasonFunction) {
		seasonMapper = seasonFunction;
	}
	
	static public Function<World, Tuple<ISeasonProvider, SeasonGrowthCalculator> > getSeasonMapper() {
		return seasonMapper;
	}
	
	static public void clearMapping() {
		seasonContextMap.clear();
	}
	
	static private Tuple<ISeasonProvider, SeasonGrowthCalculator> createSeasonProvider(World world) {
		return seasonMapper.apply(world);
	}
	
	
	////////////////////////////////////////////////////////////////
	// Main Work
	////////////////////////////////////////////////////////////////
	
	static private SeasonContext getSeasonContext(World world) {
		return seasonContextMap.computeIfAbsent(world.provider.getDimension(), d -> {
			Tuple<ISeasonProvider, SeasonGrowthCalculator> tuple = createSeasonProvider(world);
			return new SeasonContext(tuple.getFirst(), tuple.getSecond());	
		});
	}
	
	static public void updateTick(World world, long worldTicks) {
		getSeasonContext(world).updateTick(world, worldTicks);
	}
	
	static public float getSeasonValue(World world) {
		return getSeasonContext(world).getSeasonProvider().getSeasonValue(world);
	}
	
	static public void setSeasonProvider(World world, ISeasonProvider provider, SeasonGrowthCalculator calculator) {
		seasonContextMap.put(world.provider.getDimension(), new SeasonContext(provider, calculator));
	}
	
	static public void setSeasonProvider(World world, ISeasonProvider provider) {
		setSeasonProvider(world, provider, new SeasonGrowthCalculator());
	}

	
	////////////////////////////////////////////////////////////////
	// Global Season Function
	////////////////////////////////////////////////////////////////
	
	static private final float TROPICAL_THRESHHOLD = 0.85f; 
	
	static private BiPredicate<World, BlockPos> isTropical = (world, rootPos) -> world.getBiome(rootPos).getDefaultTemperature() > TROPICAL_THRESHHOLD;

	static private BiFunction<World, BlockPos, Float> globalSeasonalGrowthRateFunction = (world, rootPos) -> {
		SeasonContext context = getSeasonContext(world);
		return isTropical.test(world, rootPos) ? context.getTropicalValue() : context.getTemperateValue();
	};
	
	/**
	 * Set the global predicate that determines if a world location is tropical.
	 * Predicate should return true if tropical, false if temperate.
	 */
	static public void setGlobalTropicalPredicate(BiPredicate<World, BlockPos> predicate) {
		isTropical = predicate;
	}
	
	/** Maybe you don't like the global function season function.  Fine, do it all yourself then! */
	static public void setGlobalSeasonalGrowthRateFunction(BiFunction<World, BlockPos, Float> function) {
		globalSeasonalGrowthRateFunction = function;
	}
	
	static public float globalSeasonalGrowthRate(World world, BlockPos rootPos) {
		return globalSeasonalGrowthRateFunction.apply(world, rootPos);
	}
	
}
