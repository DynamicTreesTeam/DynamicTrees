package com.dtteam.dynamictrees.deserialization.math;

public class MaxOperator implements MathOperator {
	
	private final MathOperator[] functions;
	
	public MaxOperator(MathOperator[] functionArray) {
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		Double r = null;
		for (MathOperator f : functions) {
			double v = f.apply(mc);
			r = r == null ? v : Math.max(r, v);
		}
		
		return r == null ? 0.0 : r;
	}
}
