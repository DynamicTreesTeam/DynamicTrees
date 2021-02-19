package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAnimationHandler {
	String getName();
	void initMotion(FallingTreeEntity entity);
	void handleMotion(FallingTreeEntity entity);
	void dropPayload(FallingTreeEntity entity);
	boolean shouldDie(FallingTreeEntity entity);
	
	@OnlyIn(Dist.CLIENT)
	void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack);
	
	@OnlyIn(Dist.CLIENT)
	boolean shouldRender(FallingTreeEntity entity);

}