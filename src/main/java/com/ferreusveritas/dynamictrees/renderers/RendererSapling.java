package com.ferreusveritas.dynamictrees.renderers;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class RendererSapling implements ISimpleBlockRenderingHandler {

	public static int id;
	
	public RendererSapling() {
		id = RenderingRegistry.getNextAvailableRenderId();
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		// TODO Auto-generated method stub	
	}	
	
	@Override
	public boolean renderWorldBlock(IBlockAccess blockAccess, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

		BlockPos pos = new BlockPos(x, y, z);
		BlockDynamicSapling sapling = (BlockDynamicSapling) block;
		DynamicTree tree = sapling.getTree(pos.getBlockState(blockAccess));
		
		renderSapling(blockAccess, x, y, z, tree, renderer, 0);

		return true;
	}

	public static void renderSapling(IBlockAccess blockAccess, int x, int y, int z, DynamicTree tree, RenderBlocks renderer, double yOffset) {
		//Draw trunk
		renderer.setRenderBounds(0.4375, 0.0 + yOffset, 0.4375, 0.5625, 0.3125 + yOffset, 0.5625);
		renderer.renderStandardBlock(tree.getDynamicSapling().getBlock(), x, y, z);
		
		//Draw leaves
		renderer.setRenderBounds(0.25, 0.25 + yOffset, 0.25, 0.75, 0.75 + yOffset, 0.75);
		renderer.setOverrideBlockTexture(tree.getPrimitiveLeaves().getIcon(0));
		int multiplier = tree.foliageColorMultiplier(blockAccess, x, y, z);
		
		float r = (multiplier >> 16 & 255) / 255.0F;
		float g = (multiplier >> 8 & 255) / 255.0F;
		float b = (multiplier & 255) / 255.0F;

		renderer.renderStandardBlockWithColorMultiplier(tree.getPrimitiveLeaves().getBlock(), x, y, z, r, g, b);
		renderer.clearOverrideBlockTexture();
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
