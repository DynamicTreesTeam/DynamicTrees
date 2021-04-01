package com.ferreusveritas.dynamictrees.render;

import java.util.List;

import com.ferreusveritas.dynamictrees.trees.Species;
import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.ferreusveritas.dynamictrees.models.ModelEntityFallingTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
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
		
		if(!entity.isClientBuilt() || !entity.shouldRender()) {
			return;
		}
		
		bindEntityTexture(entity);
		
		ModelEntityFallingTree treeModel = ModelTrackerCacheEntityFallingTree.getModel(entity);
		
		int brightnessIn = ModelEntityFallingTree.getBrightness(entity);
		
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		if(entity.onFire) {
			renderFire();
		}
		
		entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks);
		this.drawBakedQuads(treeModel.getQuadData(), brightnessIn, entity.getDestroyData().species, entity);
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}
	
	private void renderFire() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.5f, 0.0f, -0.5f);
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState fire = Blocks.FIRE.getDefaultState();
		IBakedModel model = dispatcher.getModelForState(fire);
		drawBakedQuads(ModelEntityFallingTree.toTreeQuadData(QuadManipulator.getQuads(model, fire), 0xFFFFFFFF, fire), 255, null, null);
		GlStateManager.popMatrix();
	}
	
	//TODO: Convert to IBakedModel and eliminate this mess
	public void drawBakedQuads(List<ModelEntityFallingTree.TreeQuadData> inQuads, int brightness, Species species, EntityFallingTree entity) {
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		
		for (ModelEntityFallingTree.TreeQuadData treeQuad: inQuads) {
			int color = species==null ? treeQuad.color : species.colorTreeQuads(treeQuad.color, treeQuad, entity);
			this.drawBakedQuad(buffer, treeQuad.bakedQuad, brightness, color);
		}
		
		tessellator.draw();
	}
	
	public void drawBakedQuad (BufferBuilder buffer, BakedQuad bakedQuad, int brightness, int color) {
		buffer.addVertexData(bakedQuad.getVertexData());
		buffer.putBrightness4(brightness, brightness, brightness, brightness);

		if (bakedQuad.hasTintIndex()) {
			float r = (float)(color >> 16 & 255) / 255.0F;
			float g = (float)(color >> 8 & 255) / 255.0F;
			float b = (float)(color & 255) / 255.0F;
			if(bakedQuad.shouldApplyDiffuseLighting()) {
				float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedQuad.getFace());
				r *= diffuse;
				g *= diffuse;
				b *= diffuse;
			}
			buffer.putColorMultiplier(r, g, b, 4);
			buffer.putColorMultiplier(r, g, b, 3);
			buffer.putColorMultiplier(r, g, b, 2);
			buffer.putColorMultiplier(r, g, b, 1);
		} else if(bakedQuad.shouldApplyDiffuseLighting()) {
			float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedQuad.getFace());
			buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
			buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
			buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
			buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
		}
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

