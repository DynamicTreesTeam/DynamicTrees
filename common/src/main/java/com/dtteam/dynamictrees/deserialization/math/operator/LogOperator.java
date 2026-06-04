package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

import java.util.function.Function;

public class LogOperator implements MathOperator {

	private static final double LOG2 = Math.log(2);

	private final MathOperator base;
	private final MathOperator value;

	public LogOperator(MathOperator[] functionArray) {
		int numArgs = functionArray.length;
		this.value = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.base = numArgs > 1 ? functionArray[1] : mc -> Math.E;
	}

	@Override
	public double apply(MathContext mc) {
		double x = value.apply(mc);
		double b = base.apply(mc);
		if (b == Math.E) return Math.log(x);
		if (b == 10) return Math.log10(x);
		if (b == 2) return Math.log(x) / LOG2;
		return Math.log(x) / Math.log(b);
	}

}