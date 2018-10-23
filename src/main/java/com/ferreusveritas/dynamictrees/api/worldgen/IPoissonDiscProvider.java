package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.List;

import com.ferreusveritas.dynamictrees.util.PoissonDisc;

import net.minecraft.world.World;

public interface IPoissonDiscProvider {
	
	List<PoissonDisc> getPoissonDiscs(World world, int chunkX, int chunkY, int chunkZ);
	
	byte[] getChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ);
	
	void setChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ, byte[] circleData);
	
	void unloadChunkPoissonData(World world, int chunkX, int chunkY, int chunkZ);
	
}
