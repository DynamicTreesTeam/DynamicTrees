package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class NullOperator implements MathOperator {
	
	public static final MathOperator NULL = new NullOperator();
	
	public double apply(MathContext mc) {
		return 0;
	}
}
