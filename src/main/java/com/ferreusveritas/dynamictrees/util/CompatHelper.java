package com.ferreusveritas.dynamictrees.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class CompatHelper {
	
	public static boolean spawnEntity(World world, Entity entity) {
		return world.spawnEntityInWorld(entity);
	}

}
