package com.ferreusveritas.dynamictrees.api.backport;

public enum EnumDyeColor {

    WHITE(0, 15, "white"),
    ORANGE(1, 14, "orange"),
    MAGENTA(2, 13, "magenta"),
    LIGHT_BLUE(3, 12, "light_blue"),
    YELLOW(4, 11, "yellow"),
    LIME(5, 10, "lime"),
    PINK(6, 9, "pink"),
    GRAY(7, 8, "gray"),
    SILVER(8, 7, "silver"),
    CYAN(9, 6, "cyan"),
    PURPLE(10, 5, "purple"),
    BLUE(11, 4, "blue"),
    BROWN(12, 3, "brown"),
    GREEN(13, 2, "green"),
    RED(14, 1, "red"),
    BLACK(15, 0, "black");
	
    private final int meta;
    private final int dyeDamage;
    private final String name;
    
    private EnumDyeColor(int meta, int dyeDamage, String name) {
        this.meta = meta;
        this.dyeDamage = dyeDamage;
        this.name = name;
    }
    
    public int getMetadata() {
        return this.meta;
    }
    
	public int getDyeDamage() {
		return this.dyeDamage;
	}
    
	public String getName() {
    	return this.name;
	}
    
}
