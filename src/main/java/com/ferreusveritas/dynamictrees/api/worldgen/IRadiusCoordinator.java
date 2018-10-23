package com.ferreusveritas.dynamictrees.api.worldgen;

import net.minecraft.world.World;

public interface IRadiusCoordinator {

	int getRadiusAtCoords(World world, double x, double z);
	
}
