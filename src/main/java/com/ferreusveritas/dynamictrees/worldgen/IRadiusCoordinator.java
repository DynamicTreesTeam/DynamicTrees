package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.api.backport.World;

public interface IRadiusCoordinator {

	int getRadiusAtCoords(World world, double x, double z);
	
}
