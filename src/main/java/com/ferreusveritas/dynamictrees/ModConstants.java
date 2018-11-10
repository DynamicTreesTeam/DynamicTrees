package com.ferreusveritas.dynamictrees;

public class ModConstants {
	
	public static final String MODID = "dynamictrees";
	public static final String NAME = "Dynamic Trees";
	public static final String VERSION = "1.12.2-9999.9999.9999z";//Maxed out version to satisfy dependencies during dev, Assigned from gradle during build, do not change
	
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
	public static final String DYNAMICTREESBOP_VER = AT + "1.3.2" + ORGREATER;
	public static final String DYNAMICTREESTC_VER =  AT + "1.1.2" + ORGREATER;
	
	public static final String DEPENDENCIES
		= BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		;
	
}
