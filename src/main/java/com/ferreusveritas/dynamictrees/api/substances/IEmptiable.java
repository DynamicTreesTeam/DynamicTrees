package com.ferreusveritas.dynamictrees.api.substances;

import net.minecraft.item.ItemStack;

/**
 * An emptiable is a container that contains a substance that when consumed leaves a reusable container.  Such as a potion and a glass bottle.
 * 
 * @author ferreusveritas
 *
 */
public interface IEmptiable {

	/**
	 * The container item this object returns when a substance is emptied
	 * 
	 * @return
	 */
	public ItemStack getEmptyContainer();
	
}
