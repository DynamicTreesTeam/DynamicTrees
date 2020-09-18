package com.ferreusveritas.dynamictrees.models;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTrackerCacheEntityFallingTree {
	
	public static Map<Integer, ModelEntityFallingTree> modelMap = new ConcurrentHashMap<>();
	
	public static ModelEntityFallingTree getModel(EntityFallingTree entity) {
		return modelMap.computeIfAbsent(entity.getEntityId(), e -> new ModelEntityFallingTree(entity) );
	}
	
	private static int cleanupCounter = 0;
	
	public static void cleanupModels(World world, EntityFallingTree entity) {
		modelMap.remove(entity.getEntityId());//Ideally each tree should remove itself and the list is kept tidy
		
		if(++cleanupCounter >= 10) {//Every 10 cleanups check the list to see if there's any stragglers
			cleanupCounter = 0;
			Iterator<Integer> iter = modelMap.keySet().iterator();
			
			while(iter.hasNext()) {
				int id = iter.next();
				if(world.getEntityByID(id) == null) {
					modelMap.remove(id);
				}
			}
		}
	}

}
