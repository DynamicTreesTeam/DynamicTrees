package com.ferreusveritas.dynamictrees.render;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

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
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFallingTree extends Render<EntityFallingTree>{
	
	private VertexBuffer VBO = null;
	private static boolean initVBO = false;
	
	protected RenderFallingTree(RenderManager renderManager) {
		super(renderManager);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityFallingTree entity) {
		return null;
	}
	
	private void generateVBO() {
		if (this.VBO != null) {
			this.VBO.deleteGlBuffers();
		}
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.VBO = new VertexBuffer(DefaultVertexFormats.BLOCK);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		this.generateSomething(bufferbuilder);
		bufferbuilder.finishDrawing();
		bufferbuilder.reset();
		this.VBO.bufferData(bufferbuilder.getByteBuffer());
	}
	
	private void generateSomething(BufferBuilder bufferbuilder) {
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = Blocks.STONE.getDefaultState();
		IBakedModel model = dispatcher.getModelForState(state);
		renderBlockModel(model, state, new Vec3d(0, 0, 0), 255, bufferbuilder, everyFace, 0);
	}
	
	private void renderSomething() {
		GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		this.VBO.bindBuffer();
		this.setupArrayPointers();
		this.VBO.drawArrays(GL11.GL_QUADS);
		this.VBO.unbindBuffer();
		GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}
	
	private void setupArrayPointers() {
		int stride = DefaultVertexFormats.BLOCK.getNextOffset();
		GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
		GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 12);
		GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 20);
		//OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, stride, 24);
		//OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	
	@Override
	public void doRender(EntityFallingTree entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		initVBO = false;
		
		if(!initVBO) {
			generateVBO();
			initVBO = true;
		}
		
		if(!entity.isClientBuilt()) {
			return;
		}
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BranchDestructionData destructionData = entity.getDestroyData();
		BlockPos cutPos = destructionData.cutPos;
		EnumFacing cutDir = destructionData.cutDir;
		World world = entity.getEntityWorld();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		renderSomething();
		
		EntityFallingTree.animationHandler.renderTransform(entity, entityYaw, partialTicks);
		
		IExtendedBlockState exState = destructionData.getNumBranches() > 0 ? destructionData.getBranchBlockState(0) : null;
		int leavesColor = destructionData.species.getLeavesProperties().foliageColorMultiplier(destructionData.species.getLeavesProperties().getDynamicLeavesState(), world, destructionData.cutPos);
		
		//exState = null;
		
		if(exState != null) {
			bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
			
			//Draw the ring texture cap on the cut block
			int brightnessIn = exState.getPackedLightmapCoords(world, cutPos);
			for(EnumFacing face: EnumFacing.VALUES) {
				exState = exState.withProperty(BlockBranchBasic.CONNECTIONS[face.getIndex()], face == cutDir.getOpposite() ? 8 : 0);
			}
			float offset = (8 - ((BlockBranch) exState.getBlock()).getRadius(exState)) / 16f;
			IBakedModel model = dispatcher.getModelForState(exState.getClean());
			renderBlockModel(model, exState, new Vec3d(BlockPos.ORIGIN.offset(cutDir)).scale(offset), brightnessIn, bufferbuilder, new EnumFacing[] { cutDir }, 0);
			
			//Draw the rest of the tree/branch
			for(int index = 0; index < destructionData.getNumBranches(); index++) {
				exState = destructionData.getBranchBlockState(index);
				BlockPos relPos = destructionData.getBranchRelPos(index);
				model = dispatcher.getModelForState(exState.getClean());
				renderBlockModel(model, exState, new Vec3d(relPos), brightnessIn, bufferbuilder, everyFace, 0);
			}
			
			//Draw the leaves
			IBlockState state = destructionData.species.getLeavesProperties().getDynamicLeavesState();
			for(int index = 0; index < destructionData.getNumLeaves(); index++) {
				BlockPos relPos = destructionData.getLeavesRelPos(index);
				model = dispatcher.getModelForState(state);
				renderBlockModel(model, state, new Vec3d(relPos), brightnessIn, bufferbuilder, everyFace, leavesColor);
			}
			
			tessellator.draw();
		}
		
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}
	
	public static final EnumFacing everyFace[] = { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
	
	public void renderBlockModel(IBakedModel modelIn, IBlockState stateIn, Vec3d offset, int brightnessIn, BufferBuilder buffer, EnumFacing[] sides, int color) {
		
		if(stateIn != null) {
			for (EnumFacing enumfacing : sides) {
				for(BakedQuad bakedquad: modelIn.getQuads(stateIn, enumfacing, 0)) {
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
					
					buffer.putPosition(offset.x, offset.y, offset.z);
				}
			}
		}
		
	}
	
	public static class Factory implements IRenderFactory<EntityFallingTree> {
		
		@Override
		public Render<EntityFallingTree> createRenderFor(RenderManager manager) {
			return new RenderFallingTree(manager);
		}
		
	}
	
}

