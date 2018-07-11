package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.api.client.IColorProvider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * These color providers are here to be optionally overridden by
 * a season mod or whatever. 
 * 
 * @author ferreusveritas
 *
 */
@SideOnly(Side.CLIENT)
public class ColorProviders {
	
	public static IColorProvider basicFoliageColorProvider = new IColorProvider() {
		@Override
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return (world != null && pos != null) ? BiomeColorHelper.getFoliageColorAtPos(world, pos) : ColorizerFoliage.getFoliageColorBasic();
		}
	};
	
	public static IColorProvider birchFoliageColorProvider = new IColorProvider() {
		@Override
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return ColorizerFoliage.getFoliageColorBirch();
		}
	};
		
	public static IColorProvider pineFoliageColorProvider = new IColorProvider() {
		@Override
		public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
			return ColorizerFoliage.getFoliageColorPine();
		}
	};
	
}
