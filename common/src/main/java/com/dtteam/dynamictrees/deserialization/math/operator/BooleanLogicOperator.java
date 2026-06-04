package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.BooleanType;
import com.dtteam.dynamictrees.deserialization.math.ComparisonType;
import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class BooleanLogicOperator implements MathOperator {
	private final MathOperator left;
	private final MathOperator right;
	private final BooleanType operator;

	public BooleanLogicOperator(MathOperator left, MathOperator right, BooleanType operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	@Override
	public double apply(MathContext mc) {
		final boolean leftValue = isTrue(left.apply(mc));
		return switch (operator) {
			case NOT -> !isTrue(right.apply(mc));
			case AND -> leftValue && isTrue(right.apply(mc));
			case OR -> leftValue || isTrue(right.apply(mc));
			case XOR -> leftValue ^ isTrue(right.apply(mc));
		} ? 1.0 : 0.0;
	}

	public static boolean isTrue(double value){
		return value >= 0.5;
	}
}
