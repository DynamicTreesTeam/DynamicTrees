package com.ferreusveritas.dynamictrees.models;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

public class RootyStateMapper implements IStateMapper {
	
	protected Map<IBlockState, ModelResourceLocation> modelLocations = Maps.<IBlockState, ModelResourceLocation>newLinkedHashMap();
	
	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
		UnmodifiableIterator unmodifiableiterator = blockIn.getBlockState().getValidStates().iterator();

        while (unmodifiableiterator.hasNext()) {
            IBlockState iblockstate = (IBlockState) unmodifiableiterator.next();
            this.modelLocations.put(iblockstate, new ModelResourceLocation(blockIn.getRegistryName(), "roots"));
        }
		
		return modelLocations;
	}

}
