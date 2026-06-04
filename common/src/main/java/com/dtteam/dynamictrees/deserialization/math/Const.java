package com.dtteam.dynamictrees.deserialization.math;

public class Const implements MathOperator {
	private final double value;
	
	public Const(double value) {
		this.value = value;
	}
	
	@Override
	public double apply(MathContext mc) {
		return value;
	}
}
