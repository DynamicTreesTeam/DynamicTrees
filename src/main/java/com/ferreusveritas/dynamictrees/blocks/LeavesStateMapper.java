package com.ferreusveritas.dynamictrees.blocks;

import net.minecraft.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

//public class LeavesStateMapper implements IStateMapper {
//
//	@Override
//	public Map<BlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
//
//		Map<BlockState,ModelResourceLocation> modelMap = new LinkedHashMap<>();
//
//		if(blockIn instanceof BlockDynamicLeaves) {
//			BlockDynamicLeaves leaves = (BlockDynamicLeaves) blockIn;
//			for(BlockState iblockstate : blockIn.getBlockState().getValidStates()) {
//				ModelResourceLocation resloc = QuadManipulator.getModelLocation(leaves.getProperties(iblockstate).getPrimitiveLeaves());
//				if(resloc != null) {
//					modelMap.put(iblockstate, resloc);
//				}
//			}
//		}
//		return modelMap;
//	}
//
//}
