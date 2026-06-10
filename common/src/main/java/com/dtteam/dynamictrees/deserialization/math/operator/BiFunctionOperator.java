package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

import java.util.function.BiFunction;

public class BiFunctionOperator implements MathOperator {

	private final MathOperator value1;
	private final MathOperator value2;
	private final BiFunction<Double, Double, Double> function;

	public BiFunctionOperator(BiFunction<Double, Double, Double> function, MathOperator[] functionArray) {
		throwIfInvalidParameterLength(functionArray.length, 2);

		this.value1 = functionArray[0];
		this.value2 = functionArray[1];
		this.function = function;
	}

	@Override
	public double apply(MathContext mc) {
		double a = value1.apply(mc);
		double b = value2.apply(mc);
		return function.apply(a, b);
	}

}