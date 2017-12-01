package com.ferreusveritas.dynamictrees.renderers;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.dynamictrees.api.backport.BlockAccessDec;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class RendererBranch implements ISimpleBlockRenderingHandler {

	public static int id;
	public static int renderFaceFlags;
	public static int renderRingSides;
	public static final int faceDown = 1;
	public static final int faceUp = 2;
	public static final int faceNorth = 4;
	public static final int faceSouth = 8;
	public static final int faceWest = 16;
	public static final int faceEast = 32;
	public static final int faceAll = 63;

	public RendererBranch() {
		id = RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		
		BlockBranch branch = (BlockBranch)block;
		float rad = 0.1875f;

		GL11.glTranslatef(-0.5F - rad, -0.5F, -0.5F);

		renderRingSides = faceUp | faceDown;
		renderer.setRenderBounds(0.25, 0.0, 0.25, 0.75, 1.0, 0.75);
		RendererHelper.renderStandardInventoryBlock(block, metadata, modelId, renderer);

		renderRingSides = faceWest | faceEast;
		renderer.setRenderBounds(0.75, 0.4375, 0.4375, 1.0, 0.5625, 0.5625);
		RendererHelper.renderStandardInventoryBlock(block, metadata, modelId, renderer);

		renderRingSides = faceNorth | faceSouth;
		renderer.setRenderBounds(0.375, 0.375, 0.75, 0.625, 0.625, 1.0);
		RendererHelper.renderStandardInventoryBlock(block, metadata, modelId, renderer);

		GL11.glTranslatef(0.5f + rad, 0, 0);

		int color = branch.getTree().getDynamicLeaves().getRenderColor(branch.getTree().getDynamicLeavesSub() << 2);
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;

		GL11.glColor4f(r, g, b, 1.0f);
		renderer.setRenderBounds(0.5 - rad, 0.5 - rad, 0.5 - rad, 0.5 + rad, 0.5 + rad, 0.5 + rad);
		IBlockState primLeaves = branch.getTree().getPrimitiveLeaves();
		RendererHelper.renderStandardInventoryBlock(primLeaves.getBlock(), primLeaves.getMeta(), modelId, renderer);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glTranslatef(-0.5f - rad, 0, 0);

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){
		BlockAccessDec access = new BlockAccessDec(world);
		
		if(block instanceof BlockBranch){
			renderFaceFlags = faceAll;
			renderRingSides = 0;
			int faceOverrides = 0;

			BlockPos pos = new BlockPos(x, y, z);
			BlockBranch branch = (BlockBranch)block;
			int radius = branch.getRadius(access, pos);

			//Survey Radii
			int radii[] = new int[6];
			int largestConnection = 0;
			int numConnections = 0;
			int sourceDir = 0;

			for(EnumFacing dir: EnumFacing.VALUES){
				int connRadius = branch.getSideConnectionRadius(access, pos, radius, dir);
				if(connRadius > 0){
					numConnections++;
					connRadius = connRadius > radius ? radius : connRadius;//Connection radius can't be larger than node radius
					int dnum = dir.ordinal();
					if(connRadius > largestConnection){
						largestConnection = connRadius;
						sourceDir = dnum;//Set "source" node
					}
					radii[dnum] = connRadius;
				}
			}

			if(largestConnection < radius){
				sourceDir = -1;//Has no source node
			}

			if(sourceDir == -1){//Block doesn't seem to have a source
				sourceDir = 0;//default to down
				radii[0] = radius;//create a fake source
				renderRingSides = faceDown;//make it have the wood rings texture
				faceOverrides = faceDown;//override the renderer to force it to render the face
			}

			if(radius == 8){//Simply render a standard block if the radius is large enough to fill the entire block
				if(numConnections == 1){
					renderRingSides = 1 << EnumFacing.getFront(sourceDir).getOpposite().ordinal();
				}
				renderer.setRenderBounds(0.0d, 0.0d, 0.0d, 1.0d, 1.0d, 1.0d);
				renderer.renderStandardBlock(block, x, y, z);
				return true;
			}

			float min, max;

			//Defunct node
			if(numConnections == 0){
				min = 0.5f - radius / 16.0f;
				max = 0.5f + radius / 16.0f;
				renderer.setRenderBounds(min, 0.0f, min, max, 1.0f, max);
				renderer.renderStandardBlock(block, x, y, z);
				return true;
			}

			//Draw Vertical UD
			renderFaceFlags = faceNorth | faceSouth | faceEast | faceWest | faceOverrides;
			if(radii[0] == radii[1] && radii[0] != 0){//Opposites are the same radius and therefore a single block
				min = 0.5f - radii[0] / 16.0f;
				max = 0.5f + radii[0] / 16.0f;
				renderer.setRenderBounds(min, 0.0d, min, max, 1.0d, max);
				renderer.renderStandardBlock(block, x, y, z);
			} else {
				if(radii[0] > 0) { //Down -Y
					min = 0.5f - radii[0] / 16.0f;
					max = 0.5f + radii[0] / 16.0f;
					renderFaceFlags |= (sourceDir == 0 ? faceUp: 0);
					renderRingSides |= (numConnections == 1 && sourceDir == 0) ? faceUp : 0;//Pay Attn!: Special ORed case.  Handle dead end branch or stump and/or missing source node
					renderer.setRenderBounds(min, 0.0d, min, max, sourceDir == 0 ? max : min, max);
					renderer.renderStandardBlock(block, x, y, z);
				}
				if(radii[1] > 0) { //Up +Y
					min = 0.5f - radii[1] / 16.0f;
					max = 0.5f + radii[1] / 16.0f;
					renderFaceFlags |= (sourceDir == 1 ? faceDown : 0);
					renderRingSides = (numConnections == 1 && sourceDir == 1) ? faceDown : 0;//Handle dead end branch or stump
					renderer.setRenderBounds(min, sourceDir == 1 ? min : max, min, max, 1.0f, max);
					renderer.renderStandardBlock(block, x, y, z);
				}
			}

			//Draw Latinal NS
			renderFaceFlags = faceDown | faceUp | faceEast | faceWest | faceOverrides;
			renderer.uvRotateSouth = 1;
			renderer.uvRotateNorth = 1;
			if(radii[2] == radii[3] && radii[2] > 1){//Opposites are the same radius and therefore a single block
				min = 0.5f - radii[2] / 16.0f;
				max = 0.5f + radii[2] / 16.0f;
				renderer.setRenderBounds(min, min, 0.0d, max, max, 1.0d);
				renderer.renderStandardBlock(block, x, y, z);
			} else {
				if(radii[2] > 0) { //North -Z
					min = 0.5f - radii[2] / 16.0f;
					max = 0.5f + radii[2] / 16.0f;
					renderFaceFlags |= (sourceDir == 2 ? faceSouth : 0);
					renderRingSides = (numConnections == 1 && sourceDir == 2) ? faceSouth : 0;//Handle dead end branch or stump
					renderer.setRenderBounds(min, min, 0.0d, max, max, sourceDir == 2 ? max : min);
					renderer.renderStandardBlock(block, x, y, z);
				}
				if(radii[3] > 0) { //South +Z
					min = 0.5f - radii[3] / 16.0f;
					max = 0.5f + radii[3] / 16.0f;
					renderFaceFlags |= (sourceDir == 3 ? faceNorth : 0);
					renderRingSides = (numConnections == 1 && sourceDir == 3) ? faceNorth : 0;//Handle dead end branch or stump
					renderer.setRenderBounds(min, min, sourceDir == 3 ? min : max, max, max, 1.0d);
					renderer.renderStandardBlock(block, x, y, z);
				}
			}
			renderer.uvRotateSouth = 0;
			renderer.uvRotateNorth = 0;

			//Draw Longinal EW
			renderFaceFlags = faceUp | faceDown | faceNorth | faceSouth | faceOverrides;
			renderer.uvRotateEast = 1;
			renderer.uvRotateWest = 1;
			renderer.uvRotateTop = 1;
			renderer.uvRotateBottom = 1;
			if(radii[4] == radii[5] && radii[4] > 1) {//Opposites are the same radius and therefore a single block
				min = 0.5f - radii[4] / 16.0f;
				max = 0.5f + radii[4] / 16.0f;
				renderer.setRenderBounds(0.0d, min, min, 1.0f, max, max);
				renderer.renderStandardBlock(block, x, y, z);
			} else {
				if(radii[4] > 0) { //West -X
					min = 0.5f - radii[4] / 16.0f;
					max = 0.5f + radii[4] / 16.0f;
					renderFaceFlags |= (sourceDir == 4 ? faceEast : 0);
					renderRingSides = (numConnections == 1 && sourceDir == 4) ? faceEast : 0;//Handle dead end branch or stump
					renderer.setRenderBounds(0.0d, min, min, sourceDir == 4 ? max : min, max, max);
					renderer.renderStandardBlock(block, x, y, z);
				}
				if(radii[5] > 0) { //East +X
					min = 0.5f - radii[5] / 16.0f;
					max = 0.5f + radii[5] / 16.0f;
					renderFaceFlags |= (sourceDir == 5 ? faceWest : 0);
					renderRingSides = (numConnections == 1 && sourceDir == 5) ? faceWest : 0;//Handle dead end branch or stump
					renderer.setRenderBounds(sourceDir == 5 ? min : max, min, min, 1.0d, max, max);
					renderer.renderStandardBlock(block, x, y, z);
				}
			}
			renderer.uvRotateEast = 0;
			renderer.uvRotateWest = 0;
			renderer.uvRotateTop = 0;
			renderer.uvRotateBottom = 0;

			return true;
		}

		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	@Override
	public int getRenderId() {
		return id;
	}

}
