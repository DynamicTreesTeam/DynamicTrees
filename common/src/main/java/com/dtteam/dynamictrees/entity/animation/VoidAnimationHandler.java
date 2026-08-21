package com.dtteam.dynamictrees.entity.animation;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;

public class VoidAnimationHandler implements AnimationHandler {

    public String getName() {
        return "void";
    }

    public boolean shouldDie(FallingTreeEntity entity) {
        return true;
    }

    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack) {
    }

    public void initMotion(FallingTreeEntity entity) {
        FallingTreeEntity.standardDropLogsPayload(entity);
        FallingTreeEntity.standardDropLeavesPayLoad(entity);
    }

    public void handleMotion(FallingTreeEntity entity) {}

    public void dropPayload(FallingTreeEntity entity) {}

//    
    public boolean shouldRender(FallingTreeEntity entity, double x, double y, double z) {
        return false;
    }

}
