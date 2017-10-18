package com.ferreusveritas.dynamictrees;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class DynamicTreesTab extends CreativeTabs {

	private ItemStack stack;

	public DynamicTreesTab(String lable) {
		super(lable);
	}

	public void setTabIconItemStack(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public ItemStack getIconItemStack() {
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getTabIconItem() {
		return stack;
	}

}
