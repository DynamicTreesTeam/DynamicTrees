package com.ferreusveritas.dynamictrees.entities.render;

import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModelTrackerCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity> {

    protected FallingTreeRenderer(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTreeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(FallingTreeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);

        if (!entity.isClientBuilt() || !entity.shouldRender()) {
            return;
        }

        this.entityRenderDispatcher.textureManager.bind(this.getTextureLocation(entity));

        final FallingTreeEntityModel treeModel = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);

        matrixStack.pushPose();

        final VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));

//		if (entity.onFire) {
//			renderFire(matrixStack, vertexBuilder);
//		}

        entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks, matrixStack);

        treeModel.renderToBuffer(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1.0F);

        matrixStack.popPose();
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

    public static class Factory implements IRenderFactory<FallingTreeEntity> {

        @Override
        public EntityRenderer<FallingTreeEntity> createRenderFor(EntityRenderDispatcher manager) {
            return new FallingTreeRenderer(manager);
        }

    }

}

