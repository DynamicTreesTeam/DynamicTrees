package com.ferreusveritas.dynamictrees.entities.render;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class FallingTreeRenderer extends EntityRenderer<EntityFallingTree> {
	
	protected FallingTreeRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityFallingTree entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}

	@Override
	public void render(EntityFallingTree entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
		super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);

		if(!entity.isClientBuilt() || !entity.shouldRender()) {
			return;
		}

		this.renderManager.textureManager.bindTexture(this.getEntityTexture(entity));

		FallingTreeEntityModel treeModel = ModelTrackerCacheEntityFallingTree.getModel(entity);

		matrixStack.push();

		IVertexBuilder vertexBuilder = buffer.getBuffer(treeModel.getRenderType(this.getEntityTexture(entity)));

//		if(entity.onFire) {
//			renderFire(matrixStack, vertexBuilder);
//		}

		entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks, matrixStack);

		int color = treeModel.getLeavesColor();
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;

		treeModel.render(matrixStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);

		matrixStack.pop();
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

	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public EntityRenderer<EntityFallingTree> createRenderFor(EntityRendererManager manager) {
			return new FallingTreeRenderer(manager);
		}
		
	}
	
}

