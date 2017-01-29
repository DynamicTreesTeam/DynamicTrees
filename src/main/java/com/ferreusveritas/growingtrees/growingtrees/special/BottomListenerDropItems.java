package com.ferreusveritas.growingtrees.special;

import java.util.Random;

import com.ferreusveritas.growingtrees.Dir;
import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BottomListenerDropItems implements IBottomListener {

	private ItemStack toDrop;
	public float chance;
	public boolean onlyEdge;
	
	public BottomListenerDropItems(ItemStack itemStack, float dropChance, boolean edge){
		toDrop = itemStack;
		chance = dropChance;
		onlyEdge = edge;
	}
	
	@Override
	public void run(World world, GrowingTree tree, int x, int y, int z, Random random) {
		
		//Don't spawn drops if this block is in a chunk that is next to an unloaded chunk
		for(Dir d: Dir.SURROUND){
			if(!world.getChunkProvider().chunkExists((x >> 4) + d.xOffset, (z >> 4) + d.zOffset)){
				return;
			}
		}
	
		//Spawn seed
		if(!onlyEdge || tree.getGrowingLeaves().getHydrationLevel(world, x, y, z) == 1){
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
