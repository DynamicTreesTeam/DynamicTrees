package com.ferreusveritas.dynamictrees.api.backport;

public enum EnumParticleTypes {

	VILLAGER_HAPPY("happyVillager"),
	VILLAGER_ANGRY("crit"),
	SPELL("spell"),
	FIREWORKS_SPARK("fireworksSpark"),
	CRIT("crit");
	
	private String name;
	
	EnumParticleTypes(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
