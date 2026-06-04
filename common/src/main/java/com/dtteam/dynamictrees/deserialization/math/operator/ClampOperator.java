package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import net.minecraft.util.Mth;

public class ClampOperator implements MathOperator {

	private final MathOperator value;
	private final MathOperator min;
	private final MathOperator max;

	public ClampOperator(MathOperator[] functionArray) {
		int numArgs = functionArray.length;
		this.value = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.min = numArgs > 1 ? functionArray[1] : NullOperator.NULL;
		this.max = numArgs > 2 ? functionArray[2] : NullOperator.NULL;
	}

	@Override
	public double apply(MathContext mc) {
		double v = value.apply(mc);
		double mn = min.apply(mc);
		double mx = max.apply(mc);
		return Mth.clamp(v, mn, mx);
	}

}