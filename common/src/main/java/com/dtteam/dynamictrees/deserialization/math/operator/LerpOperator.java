package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.deserialization.math.MathContext;
import net.minecraft.util.Mth;

public class LerpOperator implements MathOperator {

	private final MathOperator delta;
	private final MathOperator start;
	private final MathOperator end;

	public LerpOperator(MathOperator[] functionArray) {
		int numArgs = functionArray.length;
		this.delta = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.start = numArgs > 1 ? functionArray[1] : NullOperator.NULL;
		this.end = numArgs > 2 ? functionArray[2] : NullOperator.NULL;
	}

	@Override
	public double apply(MathContext mc) {
		double d = delta.apply(mc);
		double s = start.apply(mc);
		double e = end.apply(mc);
		return Mth.lerp(d, s, e);
	}

}