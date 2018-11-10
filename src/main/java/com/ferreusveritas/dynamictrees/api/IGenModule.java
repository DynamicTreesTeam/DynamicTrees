package com.ferreusveritas.dynamictrees.api;

public interface IGenModule {
	public static final int FULLGEN	 = 1 << 0;
	public static final int PREGEN 	 = 1 << 1;
	public static final int POSTGEN  = 1 << 2;
	public static final int POSTGROW = 1 << 3;
	public static final int ALL = PREGEN | POSTGEN | POSTGROW;
}
