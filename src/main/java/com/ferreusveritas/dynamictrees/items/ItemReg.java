package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.util.IRegisterable;
import net.minecraft.item.Item;

public class ItemReg extends Item implements IRegisterable {

	protected String registryName;
	
	@Override
	public void setRegistryName(String regName) {
		registryName = regName;
	}

	@Override
	public String getRegistryName() {
		return registryName;
	}

	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setUnlocalizedName(unlocalName);
	}
	
}
