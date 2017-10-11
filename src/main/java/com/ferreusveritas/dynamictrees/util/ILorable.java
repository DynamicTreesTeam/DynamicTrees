package com.ferreusveritas.dynamictrees.util;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ILorable {

	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced);
	
}
