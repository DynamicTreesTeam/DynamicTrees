package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IUnlistedProperty;

public class MimicProperty implements IUnlistedProperty<IBlockState> {
	
	public static final MimicProperty MIMIC = new MimicProperty("mimic");
	
	private final String name;
	
	public MimicProperty(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean isValid(IBlockState value) {
		return value != null;
	}
	
	@Override
	public Class<IBlockState> getType() {
		return IBlockState.class;
	}
	
	@Override
	public String valueToString(IBlockState value) {
		return value.toString();
	}
	
	public static IBlockState getDirtMimic(IBlockAccess access, BlockPos pos) {
		
		if(!ModConfigs.rootyTextureMimicry) {
			return ModBlocks.blockStates.grass;
		}
		
		final int dMap[] = {0, -1, 1};//Y-Axis depth map
		
		IBlockState mimic = Blocks.DIRT.getDefaultState();//Default to dirt in case no dirt or grass is found
		IBlockState cache[] = new IBlockState[12];//A cache so we don't need to pull the blocks from the world twice
		int i = 0;
		
		//Prioritize Grass by searching for grass first
		for (int depth : dMap) {
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				IBlockState ground = cache[i++] = access.getBlockState(pos.offset(dir).down(depth));
				if (ground.getMaterial() == Material.GRASS && ground.isBlockNormalCube() && (!(ground.getBlock() instanceof IMimic))) {
					return ground;
				}
			}
		}
		
		//Settle for other kinds of dirt
		for (i = 0; i < 12; i++) {
			IBlockState ground = cache[i];
			if(ground != mimic && ground.getMaterial() == Material.GROUND && ground.isBlockNormalCube() && !(ground.getBlock() instanceof IMimic)) {
				return ground;
			}
		}
		
		//If all else fails then just return plain ol' dirt
		return mimic;
	}
	
	public static IBlockState getSandMimic(IBlockAccess access, BlockPos pos) {

		if(!ModConfigs.rootyTextureMimicry) {
			return ModBlocks.blockStates.sand;
		}
		
		final int dMap[] = {0, -1, 1};
		
		IBlockState mimic = Blocks.SAND.getDefaultState(); // Default to sand
		
		for (int depth : dMap) {
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				IBlockState ground = access.getBlockState(pos.offset(dir).down(depth));
				if (ground.getMaterial() == Material.SAND && (!(ground.getBlock() instanceof IMimic))) {
					return ground; // Anything made of sand will do fine 
				}
			}
		}
		
		return mimic;
	}
	
	public static interface IMimic {
		IBlockState getMimic(IBlockAccess access, BlockPos pos);
	}
	
}
