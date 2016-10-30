package com.ferreusveritas.growingtrees.special;

import java.util.Random;

import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BottomListenerDropItems implements IBottomListener {

	ItemStack toDrop;
	float chance;
	
	public BottomListenerDropItems(ItemStack itemStack, float dropChance){
		toDrop = itemStack;
		chance = dropChance;
	}
	
	@Override
	public void run(World world, BlockGrowingLeaves leaves, int x, int y, int z, int subBlockNum, Random random){
		//Spawn seed
		if(leaves.getHydrationLevel(world, x, y, z) == 1){
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
