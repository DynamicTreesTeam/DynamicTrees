package com.ferreusveritas.dynamictrees.entity.animation;

import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface AnimationHandler {
    String getName();

    void initMotion(FallingTreeEntity entity);

    void handleMotion(FallingTreeEntity entity);

    void dropPayload(FallingTreeEntity entity);

    boolean shouldDie(FallingTreeEntity entity);

    @OnlyIn(Dist.CLIENT)
    void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack);

    @OnlyIn(Dist.CLIENT)
    boolean shouldRender(FallingTreeEntity entity);

}