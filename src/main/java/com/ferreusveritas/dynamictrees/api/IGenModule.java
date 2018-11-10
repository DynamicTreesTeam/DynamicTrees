package com.ferreusveritas.dynamictrees.api;

public interface IGenModule {
	public static final int PREGEN 	 = 0x1;
	public static final int GEN 	 = 0x2;
	public static final int POSTGEN  = 0x4;
	public static final int POSTGROW = 0x8;
	public static final int ALL = PREGEN | GEN | POSTGEN | POSTGROW;
}
