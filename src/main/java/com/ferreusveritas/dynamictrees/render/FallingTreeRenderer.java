package com.ferreusveritas.dynamictrees.render;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import java.util.List;

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

		int brightnessIn = FallingTreeEntityModel.getBrightness(entity);

		matrixStack.push();

		if(entity.onFire) {
			renderFire(matrixStack);
		}

		entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks, matrixStack);

		drawBakedQuads(treeModel.getQuads(), matrixStack, brightnessIn, treeModel.getLeavesColor());

		matrixStack.pop();
	}

	private void renderFire(MatrixStack matrixStack) {
		matrixStack.push();
		matrixStack.translate(-0.5f, 0.0f, -0.5f);
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState fire = Blocks.FIRE.getDefaultState();
		IBakedModel model = dispatcher.getModelForState(fire);
		drawBakedQuads(QuadManipulator.getQuads(model, fire, EmptyModelData.INSTANCE), matrixStack,255, 0xFFFFFFFF);
		matrixStack.pop();
	}
	
	//TODO: Convert to IBakedModel and eliminate this mess
	public void drawBakedQuads(List<BakedQuad> inQuads, MatrixStack matrixStack, int brightness, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		
		for(BakedQuad bakedQuad : inQuads) {
			buffer.addVertexData(matrixStack.getLast(), bakedQuad, bakedQuad.getVertexData()[0], bakedQuad.getVertexData()[1], bakedQuad.getVertexData()[2], brightness, color, false);

//			if (bakedQuad.hasTintIndex()) {
//				float r = (float)(color >> 16 & 255) / 255.0F;
//				float g = (float)(color >> 8 & 255) / 255.0F;
//				float b = (float)(color & 255) / 255.0F;
//				if(bakedQuad.applyDiffuseLighting()) {
//					float diffuse = LightUtil.diffuseLight(bakedQuad.getFace());
//					r *= diffuse;
//					g *= diffuse;
//					b *= diffuse;
//				}
//
//				buffer.color(r, g, b, 4);
//				buffer.color(r, g, b, 3);
//				buffer.color(r, g, b, 2);
//				buffer.color(r, g, b, 1);
//			} else if(bakedQuad.applyDiffuseLighting()) {
//				float diffuse = LightUtil.diffuseLight(bakedQuad.getFace());
//
//				buffer.color(diffuse, diffuse, diffuse, 4);
//				buffer.color(diffuse, diffuse, diffuse, 3);
//				buffer.color(diffuse, diffuse, diffuse, 2);
//				buffer.color(diffuse, diffuse, diffuse, 1);
//			}
			
		}
		
		tessellator.draw();
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public EntityRenderer<EntityFallingTree> createRenderFor(EntityRendererManager manager) {
			return new FallingTreeRenderer(manager);
		}
		
	}
	
}

