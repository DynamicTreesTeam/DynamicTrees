package com.ferreusveritas.dynamictrees.api.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IColorProvider {
	int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex);
}
