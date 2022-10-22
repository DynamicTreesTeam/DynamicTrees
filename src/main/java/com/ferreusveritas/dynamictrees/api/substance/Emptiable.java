package com.ferreusveritas.dynamictrees.api.substance;

import net.minecraft.world.item.ItemStack;

/**
 * An emptiable is a container that contains a substance that when consumed leaves a reusable container.  Such as a
 * potion and a glass bottle.
 *
 * @author ferreusveritas
 */
@FunctionalInterface
public interface Emptiable {

    /**
     * The container item this object returns when a substance is emptied.
     *
     * @return An {@link ItemStack} for the item.
     */
    ItemStack getEmptyContainer();

}
