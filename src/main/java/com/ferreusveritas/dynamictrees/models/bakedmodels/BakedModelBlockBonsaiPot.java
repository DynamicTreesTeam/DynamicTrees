package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockBonsaiPot implements IBakedModel {
	
	protected IBakedModel basePotModel;
	protected Map<Species, List<BakedQuad>> cachedSaplingQuads = new HashMap<>();
	
	public BakedModelBlockBonsaiPot(IBakedModel basePotModel) {
		this.basePotModel = basePotModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
		
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		
		if(side != null) {
			return quads;
		}
		
		Species species = Species.NULLSPECIES;
		BlockState potState = null;
		
		if (state != null && state.getBlock() instanceof BlockBonsaiPot) {
			species = ((BlockState) state).getValue(SpeciesProperty.SPECIES);
			potState = ((BlockState) state).getValue(BlockBonsaiPot.POT);
		}
		if( species == null || species == Species.NULLSPECIES || !(potState.getBlock() instanceof BlockFlowerPot)) {
			return quads;
		}
		
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		IBakedModel potModel = dispatcher.getModelForState(potState);
		IBakedModel saplingModel = BakedModelSapling.getModelForSapling(species);
		
		quads.addAll(potModel.getQuads(potState, side, rand));
		quads.addAll(cachedSaplingQuads.computeIfAbsent(species, s -> QuadManipulator.getQuads(saplingModel, DTRegistries.blockDynamicSapling.getDefaultState(), new Vec3d(0, 0.25, 0), rand)));
		
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
