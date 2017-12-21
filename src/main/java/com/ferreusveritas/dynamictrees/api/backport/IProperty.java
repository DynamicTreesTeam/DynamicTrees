package com.ferreusveritas.dynamictrees.api.backport;

public interface IProperty {

	int apply(int input, int meta);
	
	int read(int meta);
	
}
