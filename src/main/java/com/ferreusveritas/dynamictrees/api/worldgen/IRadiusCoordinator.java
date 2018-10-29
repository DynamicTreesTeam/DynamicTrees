package com.ferreusveritas.dynamictrees.api.worldgen;

public interface IRadiusCoordinator {
	
	int getRadiusAtCoords(int x, int z);
	
	boolean setPacking(int chunkX, int chunkZ, int iteration);
	
}
