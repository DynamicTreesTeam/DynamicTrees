package com.ferreusveritas.dynamictrees.api.substances;

import net.minecraft.item.ItemStack;

/**
 * An interface for items that can have an effect on trees when right clicked.  Such as a tree potion.
 *
 * @author ferreusveritas
 */
public interface ISubstanceEffectProvider {

    ISubstanceEffect getSubstanceEffect(ItemStack itemStack);

}