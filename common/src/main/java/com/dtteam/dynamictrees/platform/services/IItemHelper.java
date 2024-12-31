package com.dtteam.dynamictrees.platform.services;

import net.minecraft.world.item.ItemStack;

public interface IItemHelper {

    boolean canToolAxeStrip(ItemStack stack);
    boolean canToolAxeDig (ItemStack stack);

}
