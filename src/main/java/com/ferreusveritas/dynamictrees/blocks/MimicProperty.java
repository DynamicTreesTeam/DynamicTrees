package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class MimicProperty {//implements IUnlistedProperty<BlockState> {
	
	public static final MimicProperty MIMIC = new MimicProperty("mimic");
	
	private final String name;
	
	public MimicProperty(String name) {
		this.name = name;
	}
	
//	@Override
//	public String getName() {
//		return this.name;
//	}
//
//	@Override
//	public boolean isValid(BlockState value) {
//		return value != null;
//	}
//
//	@Override
//	public Class<BlockState> getType() {
//		return BlockState.class;
//	}
//
//	@Override
//	public String valueToString(BlockState value) {
//		return value.toString();
//	}
	
	public static BlockState getDirtMimic(IBlockReader reader, BlockPos pos) {
		
		if(!DTConfigs.rootyTextureMimicry.get()) {
			return DTRegistries.blockStates.grass;
		}
		
		final int dMap[] = {0, -1, 1};//Y-Axis depth map
		
		BlockState mimic = Blocks.DIRT.getDefaultState();//Default to dirt in case no dirt or grass is found
		BlockState cache[] = new BlockState[12];//A cache so we don't need to pull the blocks from the world twice
		int i = 0;
		
		//Prioritize Grass by searching for grass first
		for (int depth : dMap) {
			for (Direction dir : CoordUtils.HORIZONTALS) {
				BlockState ground = cache[i++] = reader.getBlockState(pos.offset(dir).down(depth));
				if (ground.getMaterial() == Material.EARTH && ground.isNormalCube(reader, pos)) {
					return ground;
				}
			}
		}
		
		//Settle for other kinds of dirt
		for (i = 0; i < 12; i++) {
			BlockState ground = cache[i];
			if(ground != mimic && ground.getMaterial() == Material.EARTH && ground.isNormalCube(reader, pos) && !(ground.getBlock() instanceof IMimic)) {
				return ground;
			}
		}
		
		//If all else fails then just return plain ol' dirt
		return mimic;
	}
	
	public static BlockState getSandMimic(IBlockReader access, BlockPos pos) {

		if(!DTConfigs.rootyTextureMimicry.get()) {
			return DTRegistries.blockStates.sand;
		}
		
		final int dMap[] = {0, -1, 1};
		
		BlockState mimic = Blocks.SAND.getDefaultState(); // Default to sand
		
		for (int depth : dMap) {
			for (Direction dir : CoordUtils.HORIZONTALS) {
				BlockState ground = access.getBlockState(pos.offset(dir).down(depth));
				if (ground.getMaterial() == Material.SAND) {
					return ground; // Anything made of sand will do fine 
				}
			}
		}
		return mimic;
	}
	
	public static interface IMimic {
		BlockState getMimic(IBlockReader access, BlockPos pos);
	}
	
}
