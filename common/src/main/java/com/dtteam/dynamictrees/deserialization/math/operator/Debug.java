package com.dtteam.dynamictrees.deserialization.math.operator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.deserialization.math.MathContext;

public class Debug implements MathOperator {
	
	private final MathOperator[] functions;
	
	public Debug(MathOperator[] functionArray) {
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		if (functions.length >= 1) {
			double val = functions[0].apply(mc);
            DynamicTrees.LOG.info("Json Debug Value: {}", val);
			return val;
		}
		return 0;
	}
	
}
