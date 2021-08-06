package com.ferreusveritas.dynamictrees.api;

public interface IGenFeature {

	int FULLGEN = 1 << 0;
	int PREGEN = 1 << 1;
	int POSTGEN = 1 << 2;
	int POSTGROW = 1 << 3;
	int DEFAULTS = PREGEN | POSTGEN | POSTGROW;

}
