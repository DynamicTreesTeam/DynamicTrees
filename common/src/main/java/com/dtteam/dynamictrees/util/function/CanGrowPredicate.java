package com.dtteam.dynamictrees.util.function;

//import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
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
