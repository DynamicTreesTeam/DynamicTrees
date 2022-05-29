package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.TriPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BoneMealHelper {
	private static final Map<ItemStack, TriPredicate<World, BlockPos, Species>> boneMealItems = new HashMap<>();

	public static void addItem (Item item, int meta){
		addItem(new ItemStack(item, 1, meta));
	}
	
	public static void addItem (ItemStack stack){
		addItem(stack, (w,b,s)-> true);
	}
	
	public static void addItem (ItemStack stack, TriPredicate<World, BlockPos, Species> predicate){
		stack.setCount(1);
		boneMealItems.put(stack, predicate);
	}

	public static void replacePredicate (ItemStack stack, TriPredicate<World, BlockPos, Species> predicate){
		for (Map.Entry<ItemStack, TriPredicate<World, BlockPos, Species>> entry : boneMealItems.entrySet()){
			if (entry.getKey().isItemEqual(stack)){
				entry.setValue(predicate);
				return;
			}
		}
	}
	
	public static boolean isBoneMeal (ItemStack stack, World world, BlockPos blockPos, Species species){
		for (Map.Entry<ItemStack, TriPredicate<World, BlockPos, Species>> entry : boneMealItems.entrySet()){
			if (entry.getKey().isItemEqual(stack) && entry.getValue().test(world, blockPos, species)) return true;
		}
		return false;
	}
}
