package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

/**
 * A {@link Predicate} that tests if something should happen in a {@link Biome}.
 * Mainly used as a {@link GenFeatureProperty}.
 *
 * @author Harley O'Connor
 */
public interface BiomePredicate extends Predicate<Biome> {
}
