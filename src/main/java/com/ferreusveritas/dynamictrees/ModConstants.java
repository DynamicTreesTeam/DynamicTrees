package com.ferreusveritas.dynamictrees;

public class ModConstants {

	public static final String MODID = "dynamictrees";
	public static final String VERSION = "@VERSION@";
	//public static final String VERSION = "1.12.2-0.7.8";
	
	public static final String AFTER = "after:";
	public static final String BEFORE = "before:";
	public static final String NEXT = ";";
	
	//Other Mods
	public static final String COMPUTERCRAFT = "computercraft";
	public static final String QUARK = "quark";
	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	public static final String DYNAMICTREESTC = "dynamictreestc";
	
	//Other Mods
	public static final String DYNAMICTREESBOP_VER = "@[1.1d,)";
	public static final String DYNAMICTREESTC_VER = "@[0.1b,)";
	
	public static final String DEPENDENCIES
		= AFTER + COMPUTERCRAFT
		+ NEXT
		+ AFTER + QUARK
		+ NEXT
		+ BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		;
		
	
}
