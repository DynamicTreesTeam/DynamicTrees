package com.ferreusveritas.dynamictrees.special;

import java.util.Random;

import com.ferreusveritas.dynamictrees.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.Dir;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BottomListenerDropItems implements IBottomListener {

	private ItemStack toDrop;
	public float chance;
	public boolean onlyEdge;

	public BottomListenerDropItems(ItemStack itemStack, float dropChance, boolean edge) {
		toDrop = itemStack;
		chance = dropChance;
		onlyEdge = edge;
	}

	@Override
	public void run(World world, DynamicTree tree, int x, int y, int z, Random random) {

		//Don't spawn drops if this block is in a chunk that is next to an unloaded chunk
		if(!TreeHelper.isSurroundedByExistingChunks(world, x, y, z)) {
			return;
		}
		
		//Spawn seed
		if(!onlyEdge || tree.getGrowingLeaves().getHydrationLevel(world, x, y, z) == 1) {
			EntityItem itemEntity = new EntityItem(world, x, y, z, toDrop.copy());
			itemEntity.setPosition(x,  y - 1, z);
			world.spawnEntityInWorld(itemEntity);
		}
	}

	@Override
	public float chance() {
		return chance;
	}

	@Override
	public String getName() {
		return "dropitem";
	}

}
