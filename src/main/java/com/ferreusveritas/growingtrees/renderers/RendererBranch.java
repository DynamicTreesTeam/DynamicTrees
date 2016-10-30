package com.ferreusveritas.growingtrees.renderers;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockAndMeta;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

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

	public void renderStandardInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer){
	    Tessellator tessellator = Tessellator.instance;
	    tessellator.startDrawingQuads();
	    tessellator.setNormal(0.0F, -1.0F, 0.0F);
	    renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
	    tessellator.setNormal(0.0F, 1.0F, 0.0F);
	    renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
	    tessellator.setNormal(0.0F, 0.0F, -1.0F);
	    renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
	    tessellator.setNormal(0.0F, 0.0F, 1.0F);
	    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
	    tessellator.setNormal(-1.0F, 0.0F, 0.0F);
	    renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
	    tessellator.setNormal(1.0F, 0.0F, 0.0F);
	    renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
	    tessellator.draw();
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		
		BlockBranch branch = (BlockBranch)block;
	    float rad = 0.1875f;

	    GL11.glTranslatef(-0.5F - rad, -0.5F, -0.5F);
	    
	    renderRingSides = faceUp | faceDown;
	    renderer.setRenderBounds(0.25, 0.0, 0.25, 0.75, 1.0, 0.75);
	    renderStandardInventoryBlock(block, metadata, modelId, renderer);
	    
	    renderRingSides = faceWest | faceEast;
	    renderer.setRenderBounds(0.75, 0.4375, 0.4375, 1.0, 0.5625, 0.5625);
	    renderStandardInventoryBlock(block, metadata, modelId, renderer);

	    renderRingSides = faceNorth | faceSouth;
	    renderer.setRenderBounds(0.375, 0.375, 0.75, 0.625, 0.625, 1.0);
	    renderStandardInventoryBlock(block, metadata, modelId, renderer);
	    
	    GL11.glTranslatef(0.5f + rad, 0, 0);

	    int color = branch.getGrowingLeaves().getRenderColor(branch.getGrowingLeavesSub() << 2);
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        GL11.glColor4f(r, g, b, 1.0f);
	    renderer.setRenderBounds(0.5 - rad, 0.5 - rad, 0.5 - rad, 0.5 + rad, 0.5 + rad, 0.5 + rad);
	    BlockAndMeta primLeaves = branch.getPrimitiveLeavesBlockRef();
	    renderStandardInventoryBlock(primLeaves.getBlock(), primLeaves.getMeta(), modelId, renderer);
	    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	    GL11.glTranslatef(-0.5f - rad, 0, 0);
	    
	    GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer){

		if(block instanceof BlockBranch){
			renderFaceFlags = faceAll;
			renderRingSides = 0;
			int faceOverrides = 0;

			BlockBranch branch = (BlockBranch)block;
			int radius = branch.getRadius(world, x, y, z);

			if(radius == 1 && branch.isSapling(world, x, y, z)){
				return renderSapling(world, x, y, z, branch, modelId, renderer);
			}

			//Survey Radii
			int radii[] = new int[6];
			int largestConnection = 0;
			int numConnections = 0;
			int sourceDir = 0;
			
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
				int connRadius = branch.getSideConnectionRadius(world, x, y, z, radius, dir);
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

			if(radius == 8){
				if(numConnections == 1){
					renderRingSides = 1 << ForgeDirection.getOrientation(sourceDir).getOpposite().ordinal();
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
			if(radii[2] == radii[3] && radii[2] != 0){//Opposites are the same radius and therefore a single block
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
			if(radii[4] == radii[5] && radii[4] != 0){//Opposites are the same radius and therefore a single block
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

	public boolean renderSapling(IBlockAccess blockAcces, int x, int y, int z, BlockBranch branch, int modelId, RenderBlocks renderer){
		
		//Draw trunk
		renderer.setRenderBounds(0.4375, 0.0, 0.4375, 0.5625, 0.3125, 0.5625);
		renderer.renderStandardBlock(branch, x, y, z);
		
		//Draw leaves
		renderer.setRenderBounds(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
		renderer.setOverrideBlockTexture(branch.getLeavesIcon());
		int multiplier = branch.getGrowingLeaves().colorMultiplier(blockAcces, x, y, z);
		
		float r = (multiplier >> 16 & 255) / 255.0F;
        float g = (multiplier >> 8 & 255) / 255.0F;
        float b = (multiplier & 255) / 255.0F;
		
		renderer.renderStandardBlockWithColorMultiplier(branch, x, y, z, r, g, b);
		renderer.clearOverrideBlockTexture();
		
		return true;
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
