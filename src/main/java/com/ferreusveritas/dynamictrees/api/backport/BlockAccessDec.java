package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * BlockAccess object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public class BlockAccessDec implements IBlockAccess {

	private final IBlockAccess access;
	
	public BlockAccessDec(IBlockAccess world) {
		this.access = world;
	}

	public IBlockAccess getBlockAccess() {
		return access;
	}
	
	public IBlockState getBlockState(BlockPos pos) {
		return new BlockAndMeta(access.getBlock(pos.getX(), pos.getY(), pos.getZ()), access.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
	}
	
	public boolean isAirBlock(BlockPos pos) {
		return access.isAirBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public Block getBlock(int x, int y, int z) {
		return access.getBlock(x, y, z);
	}

	public Block getBlock(BlockPos pos) {
		return access.getBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		return access.getTileEntity(x, y, z);
	}

	@Override
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int p_72802_4_) {
		return access.getLightBrightnessForSkyBlocks(x, y, z, p_72802_4_);
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		return access.getBlockMetadata(x, y, z);
	}

	@Override
	public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {
		return access.isBlockProvidingPowerTo(p_72879_1_, p_72879_2_, p_72879_3_, p_72879_4_);
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return access.isAirBlock(x, y, z);
	}

	public BiomeGenBase getBiome(BlockPos pos) {
		return getBiomeGenForCoords(pos.getX(), pos.getZ());
	}
	
	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return access.getBiomeGenForCoords(x, z);
	}

	@Override
	public int getHeight() {
		return access.getHeight();
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return access.extendedLevelsInChunkCache();
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
		return access.isSideSolid(x, y, z, side, _default);
	}
	
}
