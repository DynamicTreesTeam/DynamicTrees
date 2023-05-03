package com.ferreusveritas.dynamictrees.entity.render;

import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModelTrackerCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity> {

    public FallingTreeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTreeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if (!entity.isClientBuilt() || !entity.shouldRender()) {
            return;
        }

        RenderSystem.setShaderTexture(0, this.getTextureLocation(entity));

        final FallingTreeEntityModel treeModel = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);

        poseStack.pushPose();

        final VertexConsumer vertexBuilder = bufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));

//		if (entity.onFire) {
//			renderFire(poseStack, vertexBuilder);
//		}

        entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTick, poseStack);

        treeModel.renderToBuffer(poseStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1.0F);

        poseStack.popPose();
    }

//	private void renderFire(MatrixStack matrixStack, IVertexBuilder buffer) {
//		matrixStack.push();
//		matrixStack.translate(-0.5f, 0.0f, -0.5f);
//		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//		BlockState fire = Blocks.FIRE.getDefaultState();
//		IBakedModel model = dispatcher.getModelForState(fire);
//
//		drawBakedQuads(QuadManipulator.getQuads(model, fire, EmptyModelData.INSTANCE), matrixStack,255, 0xFFFFFFFF);
//		matrixStack.pop();
//	}

}

