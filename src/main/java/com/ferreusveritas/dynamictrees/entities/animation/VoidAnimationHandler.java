package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VoidAnimationHandler implements IAnimationHandler {
	
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
	public void initMotion(EntityFallingTree entity) {
		EntityFallingTree.standardDropLogsPayload(entity);
		EntityFallingTree.standardDropLeavesPayLoad(entity);
		entity.cleanupRootyDirt();
	}
	
	@Override
	public void handleMotion(EntityFallingTree entity) { }
	
	@Override
	public void dropPayload(EntityFallingTree entity) {	} //Payload is dropped in initMotion
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRender(EntityFallingTree entity) {
		return false;
	}
	
}
