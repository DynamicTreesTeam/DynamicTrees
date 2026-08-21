package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import com.google.gson.JsonParseException;
import net.minecraft.util.Mth;

public class ClampOperator implements MathOperator {

	private final MathOperator value;
	private final MathOperator min;
	private final MathOperator max;

	public ClampOperator(MathOperator[] functionArray) {
		throwIfInvalidParameterLength(functionArray.length, 3);

		this.value = functionArray[0];
		this.min = functionArray[1];
		this.max = functionArray[2];
	}

	public double apply(MathContext mc) {
		double v = value.apply(mc);
		double mn = min.apply(mc);
		double mx = max.apply(mc);
		return Mth.clamp(v, mn, mx);
	}

}