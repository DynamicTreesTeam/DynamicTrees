package com.ferreusveritas.dynamictrees.blocks;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWithState;
import com.google.common.collect.UnmodifiableIterator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.util.ResourceLocation;

public class LeavesStateMapper implements IStateMapper {
	
	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
		
		Map<IBlockState,ModelResourceLocation> modelMap = new LinkedHashMap<>();
		
		if(blockIn instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) blockIn;
			
			UnmodifiableIterator unmodifiableiterator = blockIn.getBlockState().getValidStates().iterator();
			
			while (unmodifiableiterator.hasNext()) {
				IBlockState iblockstate = (IBlockState)unmodifiableiterator.next();
				ILeavesProperties properties = leaves.getProperties(iblockstate);
				IBlockState primState = properties.getPrimitiveLeaves();
				ModelResourceLocation mrl = new ModelResourceLocationWithState(new ResourceLocation(ModConstants.MODID, "autoleaf"), primState);
				modelMap.put(iblockstate, mrl);
			}
			
		}
		
		return modelMap;
	}
	
}
