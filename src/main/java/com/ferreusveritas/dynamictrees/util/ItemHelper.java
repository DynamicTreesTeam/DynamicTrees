package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/**
 *
 * @author ferreusveritas
 *
 */
public class ItemHelper {
	
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
	
	public static void consumePlayerItem(EntityPlayer player, EnumHand hand, ItemStack heldItem) {
		if (heldItem.getItem() instanceof IEmptiable) {//A substance deployed from a refillable container
			if(!player.capabilities.isCreativeMode) {
				IEmptiable emptiable = (IEmptiable) heldItem.getItem();
				player.setHeldItem(hand, emptiable.getEmptyContainer());
			}
		}
		else if(heldItem.getItem() == Items.POTIONITEM) {//An actual potion
			if(!player.capabilities.isCreativeMode) {
				player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
			}
		} else {
			ItemHelper.shrinkStack(heldItem, 1); //Just a regular item like bonemeal
		}
	}
	
}
