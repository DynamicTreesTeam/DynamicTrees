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
		final boolean leftValue = left.apply(mc) > 0.5;
		final boolean rightValue = right.apply(mc) > 0.5;
		return switch (operator) {
			case NOT -> !rightValue;
			case AND -> leftValue && rightValue;
			case OR -> leftValue || rightValue;
		} ? 1.0 : 0.0;
	}
}
