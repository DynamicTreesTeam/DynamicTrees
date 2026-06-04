package com.dtteam.dynamictrees.deserialization.math;

public class ComparisonOperator implements MathOperator {
	private final MathOperator left;
	private final MathOperator right;
	private final ComparisonType operator;
	
	public ComparisonOperator(MathOperator left, MathOperator right, ComparisonType operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	@Override
	public double apply(MathContext mc) {
		final double leftValue = left.apply(mc);
		final double rightValue = right.apply(mc);
		return switch (operator) {
			case GREATER_THAN -> leftValue > rightValue;
			case GREATER_THAN_OR_EQUAL -> leftValue >= rightValue;
			case LESS_THAN -> leftValue < rightValue;
			case LESS_THAN_OR_EQUAL -> leftValue <= rightValue;
			case EQUAL -> leftValue == rightValue;
			case NOT_EQUAL -> leftValue != rightValue;
		} ? 1.0 : 0.0;
	}
}
