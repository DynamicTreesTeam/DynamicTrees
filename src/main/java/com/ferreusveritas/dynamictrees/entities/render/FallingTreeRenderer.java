package com.ferreusveritas.dynamictrees.entities.render;

import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModelTrackerCache;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity> {

    protected FallingTreeRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingTreeEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS;
    }

    @Override
    public void render(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);

        if (!entity.isClientBuilt() || !entity.shouldRender()) {
            return;
        }

        this.entityRenderDispatcher.textureManager.bind(this.getTextureLocation(entity));

        final FallingTreeEntityModel treeModel = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);

        matrixStack.pushPose();

        final IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));

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
        public EntityRenderer<FallingTreeEntity> createRenderFor(EntityRendererManager manager) {
            return new FallingTreeRenderer(manager);
        }

    }

}

