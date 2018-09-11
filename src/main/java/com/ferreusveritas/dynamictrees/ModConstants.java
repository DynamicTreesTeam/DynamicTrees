package com.ferreusveritas.dynamictrees;

public class ModConstants {
	
	public static final String MODID = "dynamictrees";
	public static final String NAME = "Dynamic Trees";
	public static final String VERSIONDEV = "1.12.2-9.9.9z";
	public static final String VERSIONAUTO = "@VERSION@";
	public static final String VERSION = VERSIONAUTO;//Change to DEV in development, AUTO for release
	
	public static final String AFTER = "after:";
	public static final String BEFORE = "before:";
	public static final String NEXT = ";";
	
	//Other mods can use this string to depend on the latest version of Dynamic Trees
	public static final String DYNAMICTREES_LATEST = MODID + "@[" + VERSION + ",)";
	
	//Other Mods
	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	public static final String DYNAMICTREESTC = "dynamictreestc";
	
	//Other Mods
	public static final String DYNAMICTREESBOP_VER = "@[1.3.1b,)";
	public static final String DYNAMICTREESTC_VER = "@[1.1.1a,)";
	
	public static final String DEPENDENCIES
		= BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		;
	
}
