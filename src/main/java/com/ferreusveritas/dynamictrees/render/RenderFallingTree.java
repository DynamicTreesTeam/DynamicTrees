package com.ferreusveritas.dynamictrees.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFallingTree extends Render<EntityFallingTree>{
	
	protected RenderFallingTree(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFallingTree entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
	
	@Override
	public void doRender(EntityFallingTree entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if(!entity.isClientBuilt()) {
			return;
		}

		bindEntityTexture(entity);
		
		FallingTreeModel treeModel = FallingTreeModelCache.getModel(entity);

		int brightnessIn = FallingTreeModel.getBrightness(entity);

		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		EntityFallingTree.animationHandler.renderTransform(entity, entityYaw, partialTicks);
		
		drawBakedQuads(treeModel.getQuads(), brightnessIn, treeModel.getLeavesColor());
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
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
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

