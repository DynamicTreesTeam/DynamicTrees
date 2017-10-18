package com.ferreusveritas.dynamictrees.worldgen;

import net.minecraft.world.World;

public interface IRadiusCoordinator {

	int getRadiusAtCoords(World world, double x, double z);
	
}
