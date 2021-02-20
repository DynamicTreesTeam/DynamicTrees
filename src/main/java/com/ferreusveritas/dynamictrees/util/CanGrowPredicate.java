package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * A {@link BiPredicate} that tests if something should grow based on the
 * {@link World} and {@link BlockPos}. Mainly used as a {@link GenFeatureProperty}.
 *
 * @author Harley O'Connor
 */
public interface CanGrowPredicate extends BiPredicate<World, BlockPos> {
}
