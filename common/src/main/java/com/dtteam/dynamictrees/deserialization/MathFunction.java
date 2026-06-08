package com.dtteam.dynamictrees.deserialization;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Deprecated
public enum MathFunction {
	CONST,
	NOISE,
	RAND,
	RADIUS,
	ADD,
	SUB,
	MUL,
	DIV,
	MOD,
	MAX,
	MIN,
	IFGT,
	SPECIES,
	DEBUG;
	
	public final String name;
	
	MathFunction() {
		this.name = toString().toLowerCase(Locale.ENGLISH);
	}
	
	@Nullable
	public static MathFunction getFunction(String findName) {
		for (MathFunction fun : MathFunction.values()) {
			if (fun.name.equals(findName)) {
				return fun;
			}
		}
		return null;
	}
	
}
