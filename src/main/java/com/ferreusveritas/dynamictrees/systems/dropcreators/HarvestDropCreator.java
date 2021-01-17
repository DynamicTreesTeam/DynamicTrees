package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class HarvestDropCreator extends DropCreator {
	ItemStack droppedItem;
	float rate;
	
	public HarvestDropCreator(ResourceLocation name, ItemStack droppedItem, float rate) {
		super(name);
		this.droppedItem = droppedItem;
		this.rate = rate;
	}
	
	@Override
	public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
		if(rate > random.nextFloat()) {
			dropList.add(droppedItem.copy());
		}
		return dropList;
	}
	
}
