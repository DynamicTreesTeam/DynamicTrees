package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class AverageOperator implements MathOperator {

	private final MathOperator[] functions;

	public AverageOperator(MathOperator[] functionArray) {
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		double r = 0D;
		for (MathOperator f : functions) {
			double v = f.apply(mc);
			r += v;
		}
		
		return r / functions.length;
	}
}
