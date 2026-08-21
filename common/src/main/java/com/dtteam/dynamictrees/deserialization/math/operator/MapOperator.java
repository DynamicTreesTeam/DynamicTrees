package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import net.minecraft.util.Mth;

public class MapOperator implements MathOperator {

	private final MathOperator delta;
	private final MathOperator fromStart;
	private final MathOperator fromEnd;
	private final MathOperator toStart;
	private final MathOperator toEnd;

	public MapOperator(MathOperator[] functionArray) {
		throwIfInvalidParameterLength(functionArray.length, 5);
		this.delta = functionArray[0];
		this.fromStart = functionArray[1];
		this.fromEnd = functionArray[2];
		this.toStart = functionArray[3];
		this.toEnd = functionArray[4];
	}

	public double apply(MathContext mc) {
		double d = delta.apply(mc);
		double s1 = fromStart.apply(mc);
		double e1 = fromEnd.apply(mc);
		double s2 = toStart.apply(mc);
		double e2 = toEnd.apply(mc);
		return Mth.clampedMap(d, s1, e1, s2, e2);
	}

}