package com.ferreusveritas.dynamictrees.blocks;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

public class LeavesStateMapper implements IStateMapper {

	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
		
		Map<IBlockState,ModelResourceLocation> modelMap = new LinkedHashMap<>();
		
		if(blockIn instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) blockIn;
			
			BlockStateMapper mapper = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper();
			
			for(int tree = 0; tree < 4; tree++) {
				IBlockState state = leaves.getDefaultState().withProperty(BlockDynamicLeaves.TREE, tree);
				ILeavesProperties properties = leaves.getProperties(state);
				
				IBlockState primState = properties.getPrimitiveLeaves();
				Map<IBlockState, ModelResourceLocation> variantMap = mapper.getVariants(primState.getBlock());
				ModelResourceLocation mrl = variantMap.get(primState);
				
				for(int iDecay = 0; iDecay < 2; iDecay++) {
					for(int hydro = 1; hydro < 5; hydro++) {
						boolean decay = iDecay == 1;
						IBlockState keyState = state.withProperty(BlockDynamicLeaves.HYDRO, hydro).withProperty(BlockLeaves.DECAYABLE, decay);
						modelMap.put(keyState, mrl);
					}
				}
			}
			
		}
		
		return modelMap;
	}
	
}
