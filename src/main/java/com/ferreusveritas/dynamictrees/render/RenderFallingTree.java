package com.ferreusveritas.dynamictrees.render;

import java.util.Map;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
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
		return null;
	}
	
	@Override
	public void doRender(EntityFallingTree entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				
		for( Map.Entry<BlockPos, IExtendedBlockState> entry : entity.getStateMap().entrySet()) {
			BlockPos relPos = entry.getKey().subtract(entity.getCutPos()); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			IBakedModel model = dispatcher.getModelForState(exState.getClean());
			
			World world = entity.getEntityWorld();
			
			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			BlockPos blockpos = new BlockPos(entity.posX, entity.posY, entity.posZ);
			double dx = x - blockpos.getX() + relPos.getX() - 0.5D;
			double dy = y - blockpos.getY() + relPos.getY() - 0.5D;
			double dz = z - blockpos.getZ() + relPos.getZ() - 0.5D;
			//GlStateManager.rotate(entityYaw, 0, 1, 0);
			GlStateManager.translate(dx, dy, dz);
			dispatcher.getBlockModelRenderer().renderModel(world, model, exState, blockpos, bufferbuilder, false, 0);
			tessellator.draw();
				
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}

	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

