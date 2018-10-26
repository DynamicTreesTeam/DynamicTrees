package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.List;

import com.ferreusveritas.dynamictrees.util.PoissonDisc;

public interface IPoissonDiscProvider {
	
	List<PoissonDisc> getPoissonDiscs(int chunkX, int chunkY, int chunkZ);
	
	byte[] getChunkPoissonData(int chunkX, int chunkY, int chunkZ);
	
	void setChunkPoissonData(int chunkX, int chunkY, int chunkZ, byte[] circleData);
	
	void unloadChunkPoissonData(int chunkX, int chunkY, int chunkZ);
	
}
