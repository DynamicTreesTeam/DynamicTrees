package com.dtteam.dynamictrees.model.entity.render;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.entity.FallingTreeEntityModelTrackerCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity, FallingTreeRenderState> {

    public FallingTreeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public FallingTreeRenderState createRenderState() {
        return new FallingTreeRenderState();
    }

    @Override
    public void extractRenderState(FallingTreeEntity entity, FallingTreeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isClientBuilt = entity.isClientBuilt();
        state.renderAnimation = (ps) -> entity.currentAnimationHandler.renderTransform(entity, entity.getYRot(partialTicks), partialTicks, ps);
        state.model = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);
    }

    public Identifier getTextureLocation() {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void submit(FallingTreeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);

        if (!state.isClientBuilt || state.model == null) return;

        poseStack.pushPose();

        state.renderAnimation.accept(poseStack);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(getTextureLocation()), (pose, vertexConsumer) ->
                state.model.renderToBuffer(pose, vertexConsumer, state.lightCoords, OverlayTexture.NO_OVERLAY));

        poseStack.popPose();

    }

}

