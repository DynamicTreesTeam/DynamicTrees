package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 * IBlockAccess object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public interface IBlockAccess extends net.minecraft.world.IBlockAccess {
	
	public IBlockState getBlockState(BlockPos pos);
	
	//public boolean isAirBlock(int x, int y, int z);
	public boolean isAirBlock(BlockPos pos);

	//public Block getBlock(int x, int y, int z);
	public Block getBlock(BlockPos pos);

	//public int getBlockMetadata(int x, int y, int z);
	public int getBlockMetadata(BlockPos pos);

	//public TileEntity getTileEntity(int x, int y, int z);
	public TileEntity getTileEntity(BlockPos pos);
	
	//public int isBlockProvidingPowerTo(x, y, z, side);
	public int isBlockProvidingPowerTo(BlockPos pos, EnumFacing side);

	//public BiomeGenBase getBiomeGenForCoords(int x, int z);
	public Biome getBiome(BlockPos pos);

	//public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default);
	boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default);
	
	//int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBlockBrightness);
	int getLightBrightnessForSkyBlocks(BlockPos pos, int minBlockBrightness);
	
}
