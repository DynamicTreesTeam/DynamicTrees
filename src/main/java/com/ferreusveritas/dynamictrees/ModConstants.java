package com.ferreusveritas.dynamictrees;

public class ModConstants {
	
	public static final String MODID = "dynamictrees";
	public static final String NAME = "Dynamic Trees";
	public static final String VERSIONDEV = "1.12.2-9.9.9z";//Maxed out version to satisfy dependencies during dev
	public static final String VERSIONAUTO = "@VERSION@";//Assigned from gradle during build
	public static final String VERSION = VERSIONDEV;//Change to VERSIONDEV in development, VERSIONAUTO for release
	
	public static final String AFTER = "after:";
	public static final String BEFORE = "before:";
	public static final String REQAFTER = "required-after:";
	public static final String REQBEFORE = "required-before:";
	public static final String NEXT = ";";
	public static final String AT = "@[";
	public static final String ORGREATER = ",)";
	
	//Other mods can use this string to depend on the latest version of Dynamic Trees
	public static final String DYNAMICTREES_LATEST = MODID + AT + VERSION + ORGREATER;
	
	//Other Mods
	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	public static final String DYNAMICTREESTC = "dynamictreestc";
	
	//Other Mod Versions
	public static final String DYNAMICTREESBOP_VER = AT + "1.3.1b" + ORGREATER;
	public static final String DYNAMICTREESTC_VER =  AT + "1.1.1a" + ORGREATER;
	
	public static final String DEPENDENCIES
		= BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		;
	
}
