package com.dtteam.dynamictrees.entity.animation;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;

public class VoidAnimationHandler implements AnimationHandler {

    @Override
    public String getName() {
        return "void";
    }

    @Override
    public boolean shouldDie(FallingTreeEntity entity) {
        return true;
    }

    @Override
    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack) {
    }

    @Override
    public void initMotion(FallingTreeEntity entity) {
        FallingTreeEntity.standardDropLogsPayload(entity);
        FallingTreeEntity.standardDropLeavesPayLoad(entity);
    }

    @Override
    public void handleMotion(FallingTreeEntity entity) {}

    @Override
    public void dropPayload(FallingTreeEntity entity) {}

    @Override
//    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(FallingTreeEntity entity, double x, double y, double z) {
        return false;
    }

}
