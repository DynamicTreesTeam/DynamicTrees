package com.ferreusveritas.dynamictrees.blocks;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

public class LeavesStateMapper implements IStateMapper {

	private static BlockStateMapper mapper;
	
	private static BlockStateMapper getMapper() {
		if(mapper == null) {
			try {
				Field modelManagerField = Minecraft.class.getDeclaredField("modelManager");
				modelManagerField.setAccessible(true);
				ModelManager mm = (ModelManager) modelManagerField.get(Minecraft.getMinecraft());
				mapper = mm.getBlockModelShapes().getBlockStateMapper();
			}
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return mapper;
	}
	
	@Override
	public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
		
		Map<IBlockState,ModelResourceLocation> modelMap = new LinkedHashMap<>();
		
		if(blockIn instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) blockIn;
			BlockStateMapper mapper	= getMapper();
			
			for(int tree = 0; tree < 4; tree++) {
				IBlockState state = leaves.getDefaultState().withProperty(BlockDynamicLeaves.TREE, tree);
				ILeavesProperties properties = leaves.getProperties(state);
				IBlockState primState = properties.getPrimitiveLeaves();
				Map<IBlockState, ModelResourceLocation> variantMap = mapper.getVariants(primState.getBlock());
				ModelResourceLocation mrl = variantMap.get(primState);
				
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
