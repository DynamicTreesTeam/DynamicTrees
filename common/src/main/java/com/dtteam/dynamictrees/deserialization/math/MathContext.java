package com.dtteam.dynamictrees.deserialization.math;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;

public record MathContext(
		Vec3i pos,
		RandomSource rand,
		Species species,
		int radius
) {

	public MathContext(RandomSource rand) {
		this(Vec3i.ZERO, rand, Species.NULL_SPECIES, 0);
	}

	public MathContext(Vec3i pos, RandomSource rand) {
		this(pos, rand, Species.NULL_SPECIES, 0);
	}

}
