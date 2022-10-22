package com.ferreusveritas.dynamictrees.entity.animation;

import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        entity.cleanupRootyDirt();
    }

    @Override
    public void handleMotion(FallingTreeEntity entity) {
    }

    @Override
    public void dropPayload(FallingTreeEntity entity) {
    } //Payload is dropped in initMotion

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(FallingTreeEntity entity) {
        return false;
    }

}
