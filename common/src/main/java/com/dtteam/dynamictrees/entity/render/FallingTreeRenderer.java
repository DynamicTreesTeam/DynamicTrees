package com.dtteam.dynamictrees.entity.render;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity, FallingTreeRenderState> {

    public FallingTreeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public FallingTreeRenderState createRenderState() {
        return new FallingTreeRenderState();
    }

//    @Override
//    public Identifier getTextureLocation(FallingTreeEntity entity) {
//        return TextureAtlas.LOCATION_BLOCKS;
//    }
//
//    @Override
//    public void render(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//
//        if (!entity.isClientBuilt() || !entity.shouldRender(entity.getX(), entity.getY(), entity.getX())) {
//            return;
//        }
//
//        RenderSystem.setShaderTexture(0, this.getTextureLocation(entity));
//
//        final FallingTreeEntityModel treeModel = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);
//        if (treeModel == null) return;
//
//        poseStack.pushPose();
//
//        final VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderTypes.entityCutout(this.getTextureLocation(entity)));
//
//        entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTick, poseStack);
//
//        treeModel.renderToBuffer(poseStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1);
//
//        poseStack.popPose();
//    }

}

