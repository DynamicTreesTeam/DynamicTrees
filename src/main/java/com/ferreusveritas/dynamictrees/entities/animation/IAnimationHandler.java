package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
	
public interface IAnimationHandler {
	String getName();
	void initMotion(EntityFallingTree entity);
	void handleMotion(EntityFallingTree entity);
	void dropPayload(EntityFallingTree entity);
	boolean shouldDie(EntityFallingTree entity);
	
	
	@SideOnly(Side.CLIENT)
	void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks);
}