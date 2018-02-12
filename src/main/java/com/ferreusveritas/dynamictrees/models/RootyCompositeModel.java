package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

public class RootyCompositeModel implements IBakedModel {
	
	protected IBakedModel rootsModel;
	
	public RootyCompositeModel(IBakedModel rootsModel) {
		this.rootsModel = rootsModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		IBlockState mimicState;
		
		if (state != null && state.getBlock() instanceof BlockRooty && state instanceof IExtendedBlockState) {
			mimicState = ((IExtendedBlockState) state).getValue(BlockRooty.MIMIC);
		} else {
			return new ArrayList<BakedQuad>();
		}
			
		Minecraft mc = Minecraft.getMinecraft();
    	BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
    	BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
    	IBakedModel mimicModel = blockModelShapes.getModelForState(mimicState);
    	
		return mimicModel.getQuads(mimicState, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return rootsModel.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}

}
