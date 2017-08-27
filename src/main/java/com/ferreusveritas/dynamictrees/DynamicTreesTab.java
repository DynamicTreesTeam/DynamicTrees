package com.ferreusveritas.dynamictrees;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
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
	public Item getTabIconItem() {
		return stack.getItem();
	}

}
