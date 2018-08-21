package com.ferreusveritas.dynamictrees;

public class ModConstants {

	public static final String MODID = "dynamictrees";
	public static final String VERSION = "@VERSION@";
	//public static final String VERSION = "1.12.2-0.8.1";
	
	public static final String AFTER = "after:";
	public static final String BEFORE = "before:";
	public static final String NEXT = ";";
	
	//Other Mods
	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	public static final String DYNAMICTREESTC = "dynamictreestc";
	
	//Other Mods
	public static final String DYNAMICTREESBOP_VER = "@[1.2,)";
	public static final String DYNAMICTREESTC_VER = "@[1.0,)";
	
	public static final String DEPENDENCIES
		= BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		;
		
}
