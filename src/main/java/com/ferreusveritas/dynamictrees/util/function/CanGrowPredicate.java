package com.ferreusveritas.dynamictrees.util.function;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.function.BiPredicate;

/**
 * A {@link BiPredicate} that tests if something should grow based on the {@link IWorld} and {@link BlockPos}. Mainly
 * used as a {@link ConfigurationProperty}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface CanGrowPredicate extends BiPredicate<LevelAccessor, BlockPos> {
}
