package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.SpeciesProperty;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.trees.Species;

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

public class BakedModelBlockBonsaiPot implements IBakedModel {
	
	protected IBakedModel basePotModel;
	protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();
	
	public BakedModelBlockBonsaiPot(IBakedModel basePotModel) {
		this.basePotModel = basePotModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		
		if(side != null) {
			return quads;
		}
		
		Species species = Species.NULLSPECIES;
		IBlockState potState = null;
		
		if (state != null && state.getBlock() instanceof BlockBonsaiPot && state instanceof IExtendedBlockState) {
			species = ((IExtendedBlockState) state).getValue(SpeciesProperty.SPECIES);
			potState = ((IExtendedBlockState) state).getValue(BlockBonsaiPot.POT);
		}
		if( species == null || species == Species.NULLSPECIES || !(potState.getBlock() instanceof BlockFlowerPot)) {
			return quads;
		}
		
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel potModel = dispatcher.getModelForState(potState);
		IBakedModel saplingModel = BakedModelSapling.getModelForSapling(species);
		
		quads.addAll(potModel.getQuads(potState, side, rand));
		quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, ModBlocks.blockDynamicSapling.getDefaultState(), new Vec3d(0, 0.25, 0), rand)));
		
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
