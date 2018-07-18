package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

public class AnimationHandlerVoid implements IAnimationHandler {
	
	@Override
	public String getName() {
		return "void";
	};
	
	@Override
	public boolean shouldDie(EntityFallingTree entity) { 
		return true;
	}
	
	@Override
	public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) { }
	
	@Override
	public void initMotion(EntityFallingTree entity) { }
	
	@Override
	public void handleMotion(EntityFallingTree entity) { }
	
	@Override
	public void dropPayload(EntityFallingTree entity) {
		EntityFallingTree.standardDropPayload(entity);
	}
	
}
