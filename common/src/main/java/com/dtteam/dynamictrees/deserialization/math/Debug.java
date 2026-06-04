package com.dtteam.dynamictrees.deserialization.math;

import org.apache.logging.log4j.LogManager;

public class Debug implements MathOperator {
	
	private final MathOperator[] functions;
	
	public Debug(MathOperator[] functionArray) {
		this.functions = functionArray;
	}
	
	@Override
	public double apply(MathContext mc) {
		if (functions.length >= 1) {
			double val = functions[0].apply(mc);
			LogManager.getLogger().debug("Json Debug Value: " + val);
			return val;
		}
		return 0;
	}
	
}
