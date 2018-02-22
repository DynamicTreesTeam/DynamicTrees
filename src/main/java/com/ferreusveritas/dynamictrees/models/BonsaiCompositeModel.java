package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BonsaiCompositeModel implements IBakedModel {
	
	protected IBakedModel basePotModel;
	protected Map<IBlockState, ArrayList<BakedQuad>> cachedSaplingQuads = Maps.<IBlockState, ArrayList<BakedQuad>>newLinkedHashMap();
	
	public BonsaiCompositeModel(IBakedModel basePotModel) {
		this.basePotModel = basePotModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {

    	List<BakedQuad> quads = new ArrayList<BakedQuad>();

		IBlockState mimicState = null;
		IBlockState potState = null;
		
		if (state != null && state.getBlock() instanceof BlockBonsaiPot && state instanceof IExtendedBlockState) {
			mimicState = ((IExtendedBlockState) state).getValue(BlockBonsaiPot.SPECIES);
			potState = ((IExtendedBlockState) state).getValue(BlockBonsaiPot.POT);
		}
		if(mimicState == null || !(mimicState.getBlock() instanceof BlockDynamicSapling) || !(potState.getBlock() instanceof BlockFlowerPot)) {
			return quads;
		}
		
		Minecraft mc = Minecraft.getMinecraft();
    	BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
    	BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
    	IBakedModel saplingModel = blockModelShapes.getModelForState(mimicState);
    	IBakedModel potModel = blockModelShapes.getModelForState(potState);
    	
    	quads.addAll(potModel.getQuads(potState, side, rand));
    	
    	if(!cachedSaplingQuads.containsKey(mimicState)) {
    		ArrayList<BakedQuad> saplingQuads = new ArrayList<BakedQuad>();
        	for(BakedQuad q: saplingModel.getQuads(mimicState, side, rand)) {
    			BakedQuad n = new BakedQuad(q.getVertexData().clone(), q.getTintIndex(), q.getFace(), q.getSprite(), q.shouldApplyDiffuseLighting(), q.getFormat());
        		int[] data = n.getVertexData();
        		for(int i = 0; i < data.length; i+=6) {
        			data[++i] = Float.floatToIntBits(Float.intBitsToFloat(data[i]) + 0.25f);//Move all of the quads by 0.25 on the +Y axis
        		}
        		saplingQuads.add(n);
        	}
        	saplingQuads.trimToSize();
        	cachedSaplingQuads.put(mimicState, saplingQuads);
    	}

		quads.addAll(cachedSaplingQuads.get(mimicState));
    	
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
