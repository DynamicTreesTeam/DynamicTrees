package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import com.dtteam.dynamictrees.deserialization.math.noise.NoiseType;

import java.util.List;

@Deprecated
public class Scale implements MathOperator {
	
	private final List<Double> parameters;
	private final Noise noise;
	
	public Scale(
		List<Double> parameters
	) {
		this.parameters = List.copyOf(parameters);
		this.noise = Noise.build(NoiseType.LEGACY);
	}
	
	public double apply(MathContext mc) {
		double noise = this.noise.apply(mc);
		return switch (parameters.size()) {
			case 0 -> noise;
			case 1 -> noise * parameters.get(0);
			case 2 -> (noise * parameters.get(0)) + parameters.get(1);
			default -> ((noise * parameters.get(0)) + parameters.get(1)) * parameters.get(2);
		};
	}
	
}
