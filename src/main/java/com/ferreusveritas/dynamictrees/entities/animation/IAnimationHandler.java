package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAnimationHandler {
	String getName();
	void initMotion(EntityFallingTree entity);
	void handleMotion(EntityFallingTree entity);
	void dropPayload(EntityFallingTree entity);
	boolean shouldDie(EntityFallingTree entity);
	
	@OnlyIn(Dist.CLIENT)
	void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks, MatrixStack matrixStack);
	
	@OnlyIn(Dist.CLIENT)
	boolean shouldRender(EntityFallingTree entity);

}