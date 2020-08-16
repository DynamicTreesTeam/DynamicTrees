package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAnimationHandler {
	String getName();
	void initMotion(EntityFallingTree entity);
	void handleMotion(EntityFallingTree entity);
	void dropPayload(EntityFallingTree entity);
	boolean shouldDie(EntityFallingTree entity);
	
	@OnlyIn(Dist.CLIENT)
	void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks);
	
	@OnlyIn(Dist.CLIENT)
	boolean shouldRender(EntityFallingTree entity);

}