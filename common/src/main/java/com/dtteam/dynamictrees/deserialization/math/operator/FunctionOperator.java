package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

import java.util.function.Function;

public class FunctionOperator implements MathOperator {

	private final MathOperator value;
	private final Function<Double, Double> function;

	public FunctionOperator(Function<Double, Double> function, MathOperator[] functionArray) {
		int numArgs = functionArray.length;
		this.value = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.function = function;
	}

	@Override
	public double apply(MathContext mc) {
		double a = value.apply(mc);
		return function.apply(a);
	}

}