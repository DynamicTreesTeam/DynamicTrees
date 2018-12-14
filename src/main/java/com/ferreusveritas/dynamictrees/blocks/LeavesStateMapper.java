package com.ferreusveritas.dynamictrees.blocks;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWrapped;

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
			ResourceLocation resloc = new ResourceLocation(ModConstants.MODID, "leaves");
			for(IBlockState iblockstate : blockIn.getBlockState().getValidStates()) {
				modelMap.put(iblockstate, new ModelResourceLocationWrapped(resloc, leaves.getProperties(iblockstate).getPrimitiveLeaves()));
			}
		}
		return modelMap;
	}
	
}
