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
		int numArgs = functionArray.length;
		this.delta = numArgs > 0 ? functionArray[0] : NullOperator.NULL;
		this.fromStart = numArgs > 1 ? functionArray[1] : NullOperator.NULL;
		this.fromEnd = numArgs > 2 ? functionArray[2] : NullOperator.NULL;
		this.toStart = numArgs > 3 ? functionArray[3] : NullOperator.NULL;
		this.toEnd = numArgs > 4 ? functionArray[4] : NullOperator.NULL;
	}

	@Override
	public double apply(MathContext mc) {
		double d = delta.apply(mc);
		double s1 = fromStart.apply(mc);
		double e1 = fromEnd.apply(mc);
		double s2 = toStart.apply(mc);
		double e2 = toEnd.apply(mc);
		return Mth.map(d, s1, e1, s2, e2);
	}

}