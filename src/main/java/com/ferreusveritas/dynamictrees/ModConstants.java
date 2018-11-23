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
	public static final String GREATERTHAN = "@(";
	public static final String ORGREATER = ",)";
	
	//Other mods can use this string to depend on the latest version of Dynamic Trees
	public static final String DYNAMICTREES_LATEST = MODID + AT + VERSION + ORGREATER;
	
	//Other Add-on Mods
	public static final String DYNAMICTREESBOP = "dynamictreesbop";
	public static final String DYNAMICTREESTC = "dynamictreestc";
	public static final String DYNAMICTREESTRAVERSE = "dttraverse";
	public static final String DYNAMICTREESHNC = "dynamictreeshnc";
	
	//Other Mod Versions
	public static final String DYNAMICTREESBOP_VER = AT + "1.4.1a" + ORGREATER;
	public static final String DYNAMICTREESTC_VER =  AT + "1.4.1a" + ORGREATER;
	public static final String DYNAMICTREESTRAVERSE_VER =  GREATERTHAN + "1.2" + ORGREATER;//Traverse Add-on has not be updated in a while and the latest 1.2 is not longer supported
	public static final String DYNAMICTREESHNC_VER =  GREATERTHAN + "1.1" + ORGREATER;//Heat and Climate Add-on has not be updated in a while and the latest 1.1 is not longer supported
	
	//Forge
	private static final String FORGE = "forge";
	public static final String FORGE_VER = FORGE + AT + "14.23.5.2768" + ORGREATER;
	
	public static final String DEPENDENCIES
		= REQAFTER + FORGE_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESBOP + DYNAMICTREESBOP_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTC + DYNAMICTREESTC_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESTRAVERSE + DYNAMICTREESTRAVERSE_VER
		+ NEXT
		+ BEFORE + DYNAMICTREESHNC + DYNAMICTREESHNC_VER
		;
	
}
