package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class IfOperator implements MathOperator {
	
	private final MathOperator[] functions;
	
	public IfOperator(MathOperator[] functionArray) {
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		if (functions.length > 2) {
			return functions[0].apply(mc) >= 0.5 ? functions[1].apply(mc) : functions[2].apply(mc);
		}
		if (functions.length > 1) {
			return functions[0].apply(mc) >= 0.5 ? functions[1].apply(mc) : 0.0;
		}
		return 0.0;
	}
}
