package com.ferreusveritas.dynamictrees.api.backport;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockLore extends ItemBlock {

	public ItemBlockLore(Block block) {
		super(block);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		
		if(field_150939_a instanceof ILorable) {
			((ILorable)field_150939_a).addInformation(stack, player, tooltip, advanced);
		}
	}
	
}
