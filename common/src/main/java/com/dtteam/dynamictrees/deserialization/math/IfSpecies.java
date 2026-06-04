package com.dtteam.dynamictrees.deserialization.math;

import com.dtteam.dynamictrees.tree.species.Species;

@Deprecated
public class IfSpecies implements MathOperator {
	
	private final MathOperator[] functions;
	private final Species species;
	
	public IfSpecies(Species species, MathOperator[] functionArray) {
		this.species = species;
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		return mc.species().equals(species) ? functions[0].apply(mc) : functions[1].apply(mc);
	}
	
}
