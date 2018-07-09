package com.ferreusveritas.dynamictrees.event;

import java.util.List;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class FallingTreeEvent extends Event {
	
	private EntityFallingTree entity;
	
	public FallingTreeEvent(EntityFallingTree treeEntity, BranchDestructionData destroyData, List<ItemStack> payload) {
		entity = treeEntity;
	}
	
	public void setEntity(EntityFallingTree treeEntity) {
		entity = treeEntity;
	}
	
	public EntityFallingTree getEntity() {
		return entity;
	}
	
}
