package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.item.Seed;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface IInteractionHelper {

    boolean canToolAxeStrip(ItemStack stack);
    boolean canToolAxeDig (ItemStack stack);
    int setSeedItemEntityLifespan (ItemEntity entityItem, Seed seed);

}
