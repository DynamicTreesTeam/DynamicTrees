package com.ferreusveritas.dynamictrees.models;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCacheFallingTree {
	
	public static Map<Integer, ModelFallingTree> modelMap = new HashMap<>();
	
	public static ModelFallingTree getModel(EntityFallingTree entity) {
		return modelMap.computeIfAbsent(entity.getEntityId(), e -> new ModelFallingTree(entity) );
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
