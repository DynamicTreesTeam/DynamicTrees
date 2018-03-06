package com.ferreusveritas.dynamictrees.tileentity;

public enum CCDataType {
	NUMBER(Number.class),
	STRING(String.class),
	BOOLEAN(Boolean.class);
	
	public final Class clazz;
	public final char identChar;
	public final String name;
			
	CCDataType(Class clazz) {
		this.clazz = clazz;
		this.name = clazz.getSimpleName();
		this.identChar = name().toLowerCase().charAt(0);
	}

	public boolean isInstance(Object obj) {
		return clazz.isInstance(obj);
	}
	
	public static CCDataType byIdent(char ident) {
		for(CCDataType ccdt : CCDataType.values()) {
			if(ccdt.identChar == ident) {
				return ccdt;
			}
		}
		return STRING;
	}
}