package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BiFunctionOperator implements MathOperator {

	private final MathOperator value1;
	private final MathOperator value2;
	private final BiFunction<Double, Double, Double> function;

	public BiFunctionOperator(BiFunction<Double, Double, Double> function, MathOperator[] functionArray) {
		int numArgs = functionArray.length;
		this.value1 = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.value2 = numArgs > 1 ? functionArray[1] : NullOperator.NULL;
		this.function = function;
	}

	@Override
	public double apply(MathContext mc) {
		double a = value1.apply(mc);
		double b = value2.apply(mc);
		return function.apply(a, b);
	}

}