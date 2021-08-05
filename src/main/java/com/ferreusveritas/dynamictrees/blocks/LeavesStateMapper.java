package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeavesStateMapper implements IStateMapper {

	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {

		Map<IBlockState, ModelResourceLocation> modelMap = new LinkedHashMap<>();

		if (blockIn instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) blockIn;
			for (IBlockState iblockstate : blockIn.getBlockState().getValidStates()) {
				ModelResourceLocation resloc = QuadManipulator.getModelLocation(leaves.getProperties(iblockstate).getPrimitiveLeaves());
				if (resloc != null) {
					modelMap.put(iblockstate, resloc);
				}
			}
		}
		return modelMap;
	}

}
