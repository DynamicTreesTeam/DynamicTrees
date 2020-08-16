package com.ferreusveritas.dynamictrees.models;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ModelTrackerCacheEntityFallingTree {

	public static Map<Integer, ModelEntityFallingTree> modelMap = new ConcurrentHashMap<>();

	public static ModelEntityFallingTree getModel(EntityFallingTree entity) {
		return modelMap.computeIfAbsent(entity.getEntityId(), e -> new ModelEntityFallingTree(entity) );
	}

	public static void cleanupModels(World world, EntityFallingTree entity) {
		modelMap.remove(entity.getEntityId());
		cleanupModels(world);
	}

	public static void cleanupModels(World world) {
		modelMap = modelMap.entrySet().stream()
			.filter( map -> world.getEntityByID(map.getKey()) != null )
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}
}
