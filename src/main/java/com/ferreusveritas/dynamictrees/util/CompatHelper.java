package com.ferreusveritas.dynamictrees.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Maintaining code between Minecraft versions can be a pain.
 * This is a compatibility layer to abstract the finer points
 * of what has recently changed from MC 1.7.10 to MC 1.12.2
 * 
 * @author ferreusveritas
 *
 */
public class CompatHelper {
	
	public static boolean spawnEntity(World world, Entity entity) {
		return world.spawnEntity(entity);
	}

	public static int getStackCount(ItemStack stack) {
		return isValid(stack) ? stack.getCount() : 0;
	}
	
	public static ItemStack setStackCount(ItemStack stack, int size) {

		if(isValid(stack)) {
			stack.setCount(size);
			return stack;
		}
		
		return emptyStack();
	}
	
	public static ItemStack growStack(ItemStack stack, int quantity) {
		
		if(isValid(stack)) {
			stack.grow(quantity);
			return stack;
		}
		
		return emptyStack();
	}
	
	public static ItemStack shrinkStack(ItemStack stack, int quantity) {
		return growStack(stack, -quantity);
	}
	
	public static ItemStack emptyStack() {
		return ItemStack.EMPTY;
	}
	
	public static boolean isValid(ItemStack stack) {
		return stack != null && stack != emptyStack();
	}
	
	public static boolean isStackEmpty(ItemStack stack) {
		return getStackCount(stack) == 0;
	}
	
}
