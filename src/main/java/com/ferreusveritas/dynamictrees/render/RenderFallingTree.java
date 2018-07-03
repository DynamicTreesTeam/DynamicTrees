package com.ferreusveritas.dynamictrees.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
		
		System.out.println("I'm rendering");
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		
		int numQuads = 0;
		
		for( Map.Entry<BlockPos, IExtendedBlockState> entry : entity.getStateMap().entrySet()) {
			BlockPos relPos = entry.getKey().subtract(entity.getCutPos()); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			IBakedModel model = dispatcher.getModelForState(exState.getClean());
			
			List<BakedQuad> quadList = new ArrayList<>();
			for (EnumFacing enumfacing : EnumFacing.values()) {
				quadList.addAll(model.getQuads(exState, enumfacing, 0));
			}
			quadList.addAll(model.getQuads(exState, null, 0));
			
			for(BakedQuad quad : quadList) {
				numQuads++;
			}
		}

		System.out.println("Number of quads: " + numQuads);
		//At this point we have a list of all of the quads
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}
