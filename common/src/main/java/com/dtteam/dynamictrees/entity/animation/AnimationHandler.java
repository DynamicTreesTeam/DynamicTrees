package com.dtteam.dynamictrees.entity.animation;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;

public interface AnimationHandler {
    String getName();

    void initMotion(FallingTreeEntity entity);

    void handleMotion(FallingTreeEntity entity);

    void dropPayload(FallingTreeEntity entity);

    boolean shouldDie(FallingTreeEntity entity);

//    
    void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack);

//    
    boolean shouldRender(FallingTreeEntity entity, double x, double y, double z);

}