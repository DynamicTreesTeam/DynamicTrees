package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

import java.util.function.Function;

public class FunctionOperator implements MathOperator {

	private final MathOperator value;
	private final Function<Double, Double> function;

	public FunctionOperator(Function<Double, Double> function, MathOperator[] functionArray) {
		throwIfInvalidParameterLength(functionArray.length, 1);
		this.value = functionArray[0];
		this.function = function;
	}

	@Override
	public double apply(MathContext mc) {
		double a = value.apply(mc);
		return function.apply(a);
	}

}