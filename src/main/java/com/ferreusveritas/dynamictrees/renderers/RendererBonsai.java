package com.ferreusveritas.dynamictrees.renderers;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class RendererBonsai implements ISimpleBlockRenderingHandler {

	public static int id;
	
	public RendererBonsai() {
		id = RenderingRegistry.getNextAvailableRenderId();
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		// TODO Auto-generated method stub
	}	
	
	@Override
	public boolean renderWorldBlock(IBlockAccess blockAccess, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState blockState = pos.getBlockState(blockAccess);
		DynamicTree tree = ((BlockBonsaiPot) block).getTree(blockState);

		//Render flowerpot
		renderBlockFlowerpot(block, x, y, z, renderer);
		
		//Render Sapling(moved up by 4 pixels)
		RendererSapling.renderSapling(blockAccess, x, y, z, tree, renderer, 4.0 / 16.0);

		return true;
	}

    /**
     * [VanillaCopy] Renders flower pot (member function modified for purpose)
     */
    public static void renderBlockFlowerpot(Block block, int x, int y, int z, RenderBlocks renderer) {
    	//Render the exterior of the flower pot
    	renderer.renderStandardBlock(block, x, y, z);

        //Prepare colors
        int colorMultiplier = block.colorMultiplier(renderer.blockAccess, x, y, z);
        float r = (colorMultiplier >> 16 & 255) / 255.0F;
        float g = (colorMultiplier >> 8 & 255) / 255.0F;
        float b = (colorMultiplier & 255) / 255.0F;

        //Prepare tessellator
    	Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z));
        tessellator.setColorOpaque_F(r, g, b);
        
        //Render pot interior
        float halfsize = 3 / 16f;
        IIcon iconSide = Blocks.flower_pot.getIcon(0, 0);//The sides of the flower pot
        renderer.renderFaceXPos(block, x - 0.5F + halfsize, y, z, iconSide);
        renderer.renderFaceXNeg(block, x + 0.5F - halfsize, y, z, iconSide);
        renderer.renderFaceZPos(block, x, y, z - 0.5F + halfsize, iconSide);
        renderer.renderFaceZNeg(block, x, y, z + 0.5F - halfsize, iconSide);
        renderer.renderFaceYPos(block, x, y - 0.5F + halfsize + 0.1875F, z, renderer.getBlockIcon(Blocks.dirt));
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
