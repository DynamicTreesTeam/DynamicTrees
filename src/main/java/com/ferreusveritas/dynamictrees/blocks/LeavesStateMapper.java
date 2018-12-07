package com.ferreusveritas.dynamictrees.blocks;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.models.ModelResourceLocationWithState;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
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
			
			for(int tree = 0; tree < 4; tree++) {
				IBlockState state = leaves.getDefaultState().withProperty(BlockDynamicLeaves.TREE, tree);
				ILeavesProperties properties = leaves.getProperties(state);
				IBlockState primState = properties.getPrimitiveLeaves();
				ModelResourceLocation mrl = new ModelResourceLocationWithState(new ResourceLocation(ModConstants.MODID, "autoleaf"), primState);
				
				if(mrl != null) {
					for(int iDecay = 0; iDecay < 2; iDecay++) {
						for(int hydro = 1; hydro < 5; hydro++) {
							boolean decay = iDecay == 1;
							IBlockState keyState = state.withProperty(BlockDynamicLeaves.HYDRO, hydro).withProperty(BlockLeaves.DECAYABLE, decay);
							modelMap.put(keyState, mrl);
						}
					}
				}
			}
			
		}
		
		return modelMap;
	}
	
}
