package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.util.ResourceLocation;

public class Registerable implements IRegisterable {

	ResourceLocation name;
	String unlocalName;
	
	@Override
	public void setRegistryName(ResourceLocation name) {
		this.name = name;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return name;
	}

	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		this.unlocalName = unlocalName;
	}

}
