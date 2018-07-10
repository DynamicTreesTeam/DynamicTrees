package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;

import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BonsaiCompositeModel implements IBakedModel {
	
	protected IBakedModel basePotModel;
	protected Map<IBlockState, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();
	
	public BonsaiCompositeModel(IBakedModel basePotModel) {
		this.basePotModel = basePotModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		
		if(side != null) {
			return quads;
		}
		
		IBlockState mimicState = null;
		IBlockState potState = null;
		
		if (state != null && state.getBlock() instanceof BlockBonsaiPot && state instanceof IExtendedBlockState) {
			mimicState = ((IExtendedBlockState) state).getValue(BlockBonsaiPot.SPECIES);
			potState = ((IExtendedBlockState) state).getValue(BlockBonsaiPot.POT);
		}
		if(mimicState == null || !(mimicState.getBlock() instanceof BlockDynamicSapling) || !(potState.getBlock() instanceof BlockFlowerPot)) {
			return quads;
		}
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel potModel = dispatcher.getModelForState(potState);
		IBakedModel saplingModel = dispatcher.getModelForState(mimicState);
		
		quads.addAll(potModel.getQuads(potState, side, rand));
		quads.addAll(cachedSaplingQuads.computeIfAbsent(mimicState, mimic -> QuadManipulator.getQuads(saplingModel, mimic, new Vec3d(0, 0.25, 0), rand)));
		
		return quads;
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return basePotModel.isAmbientOcclusion();
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
		return basePotModel.getParticleTexture();
	}
	
	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}
	
}
