package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;

public interface MathOperator {

	double apply(MathContext mc);

	default void throwIfInvalidParameterLength(int length, int expected) throws IllegalArgumentException{
		if (length != expected){
			throw new IllegalArgumentException("Expected "+ expected +" parameters, got "+length);
		}
	}

	default void throwIfInvalidParameterLengthRange(int length, int expectedMin, int expectedMax) throws IllegalArgumentException{
		if (length > expectedMax || length < expectedMin){
			throw new IllegalArgumentException("Expected between "+ expectedMin + " and "+ expectedMax +" parameters, got "+length);
		}
	}

	default void throwIfInvalidParameterLengthMin(int length, int expectedMin) throws IllegalArgumentException{
		if (length < expectedMin){
			throw new IllegalArgumentException("Expected at least "+ expectedMin + " parameters, got "+length);
		}
	}

}
