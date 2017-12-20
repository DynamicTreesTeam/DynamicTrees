package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.util.ResourceLocation;

public interface IRegisterable {
	public void setRegistryName(ResourceLocation regName);
	public ResourceLocation getRegistryName();
	public void setUnlocalizedNameReg(String unlocalName);
}
