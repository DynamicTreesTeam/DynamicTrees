package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import com.dtteam.dynamictrees.tree.species.Species;

import java.util.List;

public class IsSpecies implements MathOperator {
	
	private final List<Species> species;
	
	public IsSpecies(List<Species> species) {
		this.species = species;
	}
	
	@Override
	public double apply(MathContext mc) {
		return species.contains(mc.species()) ? 1.0 : 0.0;
	}
	
}
