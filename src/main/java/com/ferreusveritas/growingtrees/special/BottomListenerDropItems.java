package com.ferreusveritas.growingtrees.special;

import java.util.Random;

import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BottomListenerDropItems implements IBottomListener {

	ItemStack toDrop;
	float chance;
	
	public BottomListenerDropItems(ItemStack itemStack, float dropChance){
		toDrop = itemStack;
		chance = dropChance;
	}
	
	@Override
	public void run(World world, GrowingTree tree, int x, int y, int z, Random random) {
		
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		ChunkCoordIntPair p = chunk.getChunkCoordIntPair();
	
		//Don't spawn drops if the chunk is next to an unloaded chunk
		if(	!world.getChunkFromChunkCoords(p.chunkXPos + 1, p.chunkZPos + 0).isChunkLoaded ||
			!world.getChunkFromChunkCoords(p.chunkXPos - 1, p.chunkZPos + 0).isChunkLoaded ||
			!world.getChunkFromChunkCoords(p.chunkXPos + 0, p.chunkZPos + 1).isChunkLoaded ||
			!world.getChunkFromChunkCoords(p.chunkXPos + 0, p.chunkZPos - 1).isChunkLoaded ) {
			return;
		}
		
		//Spawn seed
		if(tree.getGrowingLeaves().getHydrationLevel(world, x, y, z) == 1){
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
