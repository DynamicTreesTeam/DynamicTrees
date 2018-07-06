package com.ferreusveritas.dynamictrees.render;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
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
		BlockPos cutPos = entity.getCutPos();
		World world = entity.getEntityWorld();
		Vec3d mc = entity.getMassCenter();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(mc.x + x, mc.y + y, mc.z + z);
		GlStateManager.rotate(entityYaw, 0, 1, 0);
		GlStateManager.translate(-mc.x - cutPos.getX() - 0.5, -mc.y - cutPos.getY(), -mc.z -cutPos.getZ() - 0.5);
		
		for( Map.Entry<BlockPos, IExtendedBlockState> entry : entity.getStateMap().entrySet()) {
			BlockPos pos = entry.getKey(); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			IBakedModel model = dispatcher.getModelForState(exState.getClean());
			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
			renderModelFlat(world, model, exState, cutPos, bufferbuilder, false, 0);
			tessellator.draw();
			GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
		}
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}
	
	public boolean renderModelFlat(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, boolean checkSides, long rand) {
		boolean rendered = false;
		BitSet bitset = new BitSet(3);
		
		for (EnumFacing enumfacing : EnumFacing.values()) {
			List<BakedQuad> list = modelIn.getQuads(stateIn, enumfacing, rand);
			
			if (!list.isEmpty() && (!checkSides || stateIn.shouldSideBeRendered(worldIn, posIn, enumfacing))) {
				int i = stateIn.getPackedLightmapCoords(worldIn, posIn.offset(enumfacing));
				this.renderQuadsFlat(worldIn, stateIn, posIn, i, false, buffer, list, bitset);
				rendered = true;
			}
		}
		
		List<BakedQuad> list1 = modelIn.getQuads(stateIn, (EnumFacing)null, rand);
		
		if (!list1.isEmpty()) {
			this.renderQuadsFlat(worldIn, stateIn, posIn, -1, true, buffer, list1, bitset);
			rendered = true;
		}
		
		return rendered;
	}
	
	private void renderQuadsFlat(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, int brightnessIn, boolean ownBrightness, BufferBuilder buffer, List<BakedQuad> quadList, BitSet bitSet) {
		Vec3d vec3d = stateIn.getOffset(blockAccessIn, posIn);
		double x = (double)posIn.getX() + vec3d.x;
		double y = (double)posIn.getY() + vec3d.y;
		double z = (double)posIn.getZ() + vec3d.z;
		
		for(BakedQuad bakedquad: quadList) {
			
			if (ownBrightness) {
				this.fillQuadBounds(stateIn, bakedquad.getVertexData(), bakedquad.getFace(), (float[])null, bitSet);
				BlockPos blockpos = bitSet.get(0) ? posIn.offset(bakedquad.getFace()) : posIn;
				brightnessIn = stateIn.getPackedLightmapCoords(blockAccessIn, blockpos);
			}
			
			buffer.addVertexData(bakedquad.getVertexData());
			buffer.putBrightness4(brightnessIn, brightnessIn, brightnessIn, brightnessIn);
			
			if (bakedquad.hasTintIndex()) {
				int color = 0xFFFFFFFF;
				
				float red = (float)(color >> 16 & 255) / 255.0F;
				float grn = (float)(color >> 8 & 255) / 255.0F;
				float blu = (float)(color & 255) / 255.0F;
				if(bakedquad.shouldApplyDiffuseLighting()) {
					float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
					red *= diffuse;
					grn *= diffuse;
					blu *= diffuse;
				}
				buffer.putColorMultiplier(red, grn, blu, 4);
				buffer.putColorMultiplier(red, grn, blu, 3);
				buffer.putColorMultiplier(red, grn, blu, 2);
				buffer.putColorMultiplier(red, grn, blu, 1);
			}
			else if(bakedquad.shouldApplyDiffuseLighting()) {
				float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 4);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 3);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 2);
				buffer.putColorMultiplier(diffuse, diffuse, diffuse, 1);
			}
			
			buffer.putPosition(x, y, z);
		}
	}
	
	private void fillQuadBounds(IBlockState stateIn, int[] vertexData, EnumFacing face, @Nullable float[] quadBounds, BitSet boundsFlags) {
		float x1 = 32.0F;
		float y1 = 32.0F;
		float z1 = 32.0F;
		float x2 = -32.0F;
		float y2 = -32.0F;
		float z2 = -32.0F;
		
		for (int i = 0; i < 4; ++i) {
			float xx = Float.intBitsToFloat(vertexData[i * 7]);
			float yy = Float.intBitsToFloat(vertexData[i * 7 + 1]);
			float zz = Float.intBitsToFloat(vertexData[i * 7 + 2]);
			x1 = Math.min(x1, xx);
			y1 = Math.min(y1, yy);
			z1 = Math.min(z1, zz);
			x2 = Math.max(x2, xx);
			y2 = Math.max(y2, yy);
			z2 = Math.max(z2, zz);
		}
		
		if (quadBounds != null) {
			quadBounds[EnumFacing.WEST.getIndex()] = x1;
			quadBounds[EnumFacing.EAST.getIndex()] = x2;
			quadBounds[EnumFacing.DOWN.getIndex()] = y1;
			quadBounds[EnumFacing.UP.getIndex()] = y2;
			quadBounds[EnumFacing.NORTH.getIndex()] = z1;
			quadBounds[EnumFacing.SOUTH.getIndex()] = z2;
			
			int j = EnumFacing.values().length;
			quadBounds[EnumFacing.WEST.getIndex()  + j] = 1.0F - x1;
			quadBounds[EnumFacing.EAST.getIndex()  + j] = 1.0F - x2;
			quadBounds[EnumFacing.DOWN.getIndex()  + j] = 1.0F - y1;
			quadBounds[EnumFacing.UP.getIndex()    + j] = 1.0F - y2;
			quadBounds[EnumFacing.NORTH.getIndex() + j] = 1.0F - z1;
			quadBounds[EnumFacing.SOUTH.getIndex() + j] = 1.0F - z2;
		}
				
		switch (face) {
		case DOWN:
			boundsFlags.set(1, x1 >= 0.0001f || z1 >= 0.0001f || x2 <= 0.9999F || z2 <= 0.9999F);
			boundsFlags.set(0, (y1 < 0.0001f || stateIn.isFullCube()) && y1 == y2);
			break;
		case UP:
			boundsFlags.set(1, x1 >= 0.0001f || z1 >= 0.0001f || x2 <= 0.9999F || z2 <= 0.9999F);
			boundsFlags.set(0, (y2 > 0.9999F || stateIn.isFullCube()) && y1 == y2);
			break;
		case NORTH:
			boundsFlags.set(1, x1 >= 0.0001f || y1 >= 0.0001f || x2 <= 0.9999F || y2 <= 0.9999F);
			boundsFlags.set(0, (z1 < 0.0001f || stateIn.isFullCube()) && z1 == z2);
			break;
		case SOUTH:
			boundsFlags.set(1, x1 >= 0.0001f || y1 >= 0.0001f || x2 <= 0.9999F || y2 <= 0.9999F);
			boundsFlags.set(0, (z2 > 0.9999F || stateIn.isFullCube()) && z1 == z2);
			break;
		case WEST:
			boundsFlags.set(1, y1 >= 0.0001f || z1 >= 0.0001f || y2 <= 0.9999F || z2 <= 0.9999F);
			boundsFlags.set(0, (x1 < 0.0001f || stateIn.isFullCube()) && x1 == x2);
			break;
		case EAST:
			boundsFlags.set(1, y1 >= 0.0001f || z1 >= 0.0001f || y2 <= 0.9999F || z2 <= 0.9999F);
			boundsFlags.set(0, (x2 > 0.9999F || stateIn.isFullCube()) && x1 == x2);
		}
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

