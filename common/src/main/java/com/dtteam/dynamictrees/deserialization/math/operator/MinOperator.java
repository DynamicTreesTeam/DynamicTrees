package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class MinOperator implements MathOperator {
	
	private final MathOperator[] functions;
	
	public MinOperator(MathOperator[] functionArray) {
		throwIfInvalidParameterLengthMin(functionArray.length, 1);
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		Double r = null;
		for (MathOperator f : functions) {
			double v = f.apply(mc);
			r = r == null ? v : Math.min(r, v);
		}
		
		return r == null ? 0.0 : r;
	}
}
