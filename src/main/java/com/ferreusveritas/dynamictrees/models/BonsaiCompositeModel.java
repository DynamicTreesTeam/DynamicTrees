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
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
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
		
		Minecraft mc = Minecraft.getMinecraft();
    	BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();
    	BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
    	IBakedModel saplingModel = blockModelShapes.getModelForState(mimicState);
    	IBakedModel potModel = blockModelShapes.getModelForState(potState);
    	
    	quads.addAll(potModel.getQuads(potState, side, rand));
    	
    	if(!cachedSaplingQuads.containsKey(mimicState)) {
    		ArrayList<BakedQuad> inQuads = new ArrayList<BakedQuad>();
    		ArrayList<BakedQuad> outQuads = new ArrayList<BakedQuad>();
    		
    		//Gather all of the quads for all directions and none
    		for(EnumFacing dir: EnumFacing.VALUES) {
    			inQuads.addAll(saplingModel.getQuads(mimicState, dir, rand));
    		}
    		inQuads.addAll(saplingModel.getQuads(mimicState, null, rand));
    		
        	for(BakedQuad inQuad: inQuads) {
    			BakedQuad quadCopy = new BakedQuad(inQuad.getVertexData().clone(), inQuad.getTintIndex(), inQuad.getFace(), inQuad.getSprite(), inQuad.shouldApplyDiffuseLighting(), inQuad.getFormat());
        		int[] vertexData = quadCopy.getVertexData();
        		for(int i = 0; i < vertexData.length; i += inQuad.getFormat().getIntegerSize()) {
        			int pos = 0;
            		for(VertexFormatElement vfe: inQuad.getFormat().getElements()) {
            			if(vfe.getUsage() == EnumUsage.POSITION) {
            				float y = Float.intBitsToFloat(vertexData[i + pos + 1]);
            				y += 0.25f;//Move all of the quads by 0.25 on the +Y axis
                			vertexData[i + pos + 1] = Float.floatToIntBits(y);
                			break;
            			}
            			pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
            		}
        		}
        		
        		outQuads.add(quadCopy);
        	}
        	outQuads.trimToSize();
        	cachedSaplingQuads.put(mimicState, outQuads);
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
