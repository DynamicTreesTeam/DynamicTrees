package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class MaxOperator implements MathOperator {
	
	private final MathOperator[] functions;
	
	public MaxOperator(MathOperator[] functionArray) {
		throwIfInvalidParameterLengthMin(functionArray.length, 1);
		this.functions = functionArray;
	}
	
	public double apply(MathContext mc) {
		Double r = null;
		for (MathOperator f : functions) {
			double v = f.apply(mc);
			r = r == null ? v : Math.max(r, v);
		}
		
		return r == null ? 0.0 : r;
	}
}
