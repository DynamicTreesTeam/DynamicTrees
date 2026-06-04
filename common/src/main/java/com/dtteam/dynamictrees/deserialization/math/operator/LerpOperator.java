package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import net.minecraft.util.Mth;

public class LerpOperator implements MathOperator {

	private final MathOperator delta;
	private final MathOperator start;
	private final MathOperator end;

	public LerpOperator(MathOperator[] functionArray) {
		throwIfInvalidParameterLength(functionArray.length, 3);
		this.delta = functionArray[0];
		this.start = functionArray[1];
		this.end = functionArray[2];
	}

	@Override
	public double apply(MathContext mc) {
		double d = delta.apply(mc);
		double s = start.apply(mc);
		double e = end.apply(mc);
		return Mth.lerp(d, s, e);
	}

}