package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * @author Harley O'Connor
 */
public interface CanGrowPredicate extends BiPredicate<World, BlockPos> {
}
