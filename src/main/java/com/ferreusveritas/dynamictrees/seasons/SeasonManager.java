package com.ferreusveritas.dynamictrees.seasons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.api.seasons.ISeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.api.seasons.ISeasonManager;

import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonManager implements ISeasonManager {
	
	private Map<Integer, SeasonContext> seasonContextMap = new HashMap<>();
	private Function<World, Tuple<ISeasonProvider, ActiveSeasonGrowthCalculator> > seasonMapper = w -> new Tuple(new SeasonProviderNull(), new NullSeasonGrowthCalculator());
	
	public SeasonManager() {}
	
	public SeasonManager(Function<World, Tuple<ISeasonProvider, ActiveSeasonGrowthCalculator> > seasonMapper) {
		this.seasonMapper = seasonMapper;
	}
	
	private Tuple<ISeasonProvider, ActiveSeasonGrowthCalculator> createProvider(World world) {
		return seasonMapper.apply(world);
	}

	private SeasonContext getContext(World world) {
		return seasonContextMap.computeIfAbsent(world.provider.getDimension(), d -> {
			Tuple<ISeasonProvider, ActiveSeasonGrowthCalculator> tuple = createProvider(world);
			return new SeasonContext(tuple.getFirst(), tuple.getSecond());	
		});
	}

	public void setProvider(World world, ISeasonProvider provider, ISeasonGrowthCalculator calc) {
		setProvider(world, provider, calc);
	}
	
	
	////////////////////////////////////////////////////////////////
	// Tropical Predicate
	////////////////////////////////////////////////////////////////
	
	static private final float TROPICAL_THRESHHOLD = 0.85f; 
	
	private BiPredicate<World, BlockPos> isTropical = (world, rootPos) -> world.getBiome(rootPos).getDefaultTemperature() > TROPICAL_THRESHHOLD;

	/**
	 * Set the global predicate that determines if a world location is tropical.
	 * Predicate should return true if tropical, false if temperate.
	 */
	public void setTropicalPredicate(BiPredicate<World, BlockPos> predicate) {
		isTropical = predicate;
	}

	
	////////////////////////////////////////////////////////////////
	// ISeasonManager Interface
	////////////////////////////////////////////////////////////////
	
	public void updateTick(World world, long worldTicks) {
		getContext(world).updateTick(world, worldTicks);
	}
	
	public float getGrowthRate (World world, BlockPos rootPos) {
		SeasonContext context = getContext(world);
		return isTropical.test(world, rootPos) ? context.getTropicalValue() : context.getTemperateValue();
	};
	
	public float getSeedDropRate(World world, BlockPos rootPos) {
		return getContext(world).getSeedDropRate();
	}

	public float getSeasonValue(World world) {
		return getContext(world).getSeasonProvider().getSeasonValue(world);
	}
	
}
