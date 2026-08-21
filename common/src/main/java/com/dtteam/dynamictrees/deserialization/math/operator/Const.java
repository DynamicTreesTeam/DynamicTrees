package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class Const implements MathOperator {
	private final double value;
	
	public Const(double value) {
		this.value = value;
	}
	
	public double apply(MathContext mc) {
		return value;
	}
}
