package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.ArithmeticType;
import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class ArithmeticOperator implements MathOperator {
	private final MathOperator left;
	private final MathOperator right;
	private final ArithmeticType operator;
	
	public ArithmeticOperator(MathOperator left, MathOperator right, ArithmeticType operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	public double apply(MathContext mc) {
		final double leftValue = left.apply(mc);
		final double rightValue = right.apply(mc);
		return switch (operator) {
			case ADD -> leftValue + rightValue;
			case SUB -> leftValue - rightValue;
			case MUL -> leftValue * rightValue;
			case DIV -> leftValue / rightValue;
			case MOD -> leftValue % rightValue;
		};
	}
}
