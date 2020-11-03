package com.ferreusveritas.dynamictrees.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.ModelEntityFallingTree;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class RenderFallingTree extends EntityRenderer<EntityFallingTree> {
	
	protected RenderFallingTree(EntityRendererManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFallingTree entity) {
		return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	public void doRender(EntityFallingTree entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if(!entity.isClientBuilt() || !entity.shouldRender()) {
			return;
		}
		
		bindEntityTexture(entity);
		
		ModelEntityFallingTree treeModel = ModelTrackerCacheEntityFallingTree.getModel(entity);
		
		int brightnessIn = ModelEntityFallingTree.getBrightness(entity);
		
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		
		if(entity.onFire) {
			renderFire();
		}
		
		entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks);
		
		drawBakedQuads(treeModel.getQuads(), brightnessIn, treeModel.getLeavesColor());
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}
	
	private void renderFire() {
		GlStateManager.pushMatrix();
		GlStateManager.translated(-0.5f, 0.0f, -0.5f);
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState fire = Blocks.FIRE.getDefaultState();
		IBakedModel model = dispatcher.getModelForState(fire);
		drawBakedQuads(QuadManipulator.getQuads(model, fire), 255, 0xFFFFFFFF);
		GlStateManager.popMatrix();
	}
	
	//TODO: Convert to IBakedModel and eliminate this mess
	public void drawBakedQuads(List<BakedQuad> inQuads, int brightnessIn, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		
		for(BakedQuad bakedquad: inQuads) {
			buffer.addVertexData(bakedquad.getVertexData());
			buffer.putBrightness4(brightnessIn, brightnessIn, brightnessIn, brightnessIn);
			
			if (bakedquad.hasTintIndex()) {
				float r = (float)(color >> 16 & 255) / 255.0F;
				float g = (float)(color >> 8 & 255) / 255.0F;
				float b = (float)(color & 255) / 255.0F;
				if(bakedquad.shouldApplyDiffuseLighting()) {
					float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
					r *= diffuse;
					g *= diffuse;
					b *= diffuse;
				}
				buffer.putColorMultiplier(r, g, b, 4);
				buffer.putColorMultiplier(r, g, b, 3);
				buffer.putColorMultiplier(r, g, b, 2);
				buffer.putColorMultiplier(r, g, b, 1);
			}
			else if(bakedquad.shouldApplyDiffuseLighting()) {
				float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
			}
			
		}
		
		tessellator.draw();
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public EntityRenderer<EntityFallingTree> createRenderFor(EntityRendererManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

