package com.dtteam.dynamictrees.api.worldgen;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Predicate;

/**
 * A {@link Predicate} that tests if something should happen in a {@link Biome}. Mainly used as a {@link
 * ConfigurationProperty}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface BiomePredicate extends Predicate<Holder<Biome>> {
}
