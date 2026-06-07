package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class IfOperator implements MathOperator {
	
	private final MathOperator[] functions;
	
	public IfOperator(MathOperator[] functionArray) {
		this.functions = functionArray;
		throwIfInvalidParameterLengthRange(functions.length, 2, 3);
	}
	
	@Override
	public double apply(MathContext mc) {
		if (functions.length == 3) {
			return BooleanLogicOperator.isTrue(functions[0].apply(mc)) ? functions[1].apply(mc) : functions[2].apply(mc);
		}
		if (functions.length == 2) {
			return BooleanLogicOperator.isTrue(functions[0].apply(mc)) ? functions[1].apply(mc) : 0.0;
		}
		return 0.0;
	}
}
