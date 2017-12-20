package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * BlockAccess object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public class BlockAccess implements IBlockAccess {

	private final net.minecraft.world.IBlockAccess access;
	
	public BlockAccess(net.minecraft.world.IBlockAccess world) {
		this.access = world;
	}

	public net.minecraft.world.IBlockAccess getRealBlockAccess() {
		return access;
	}
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return new BlockState(getBlock(pos), getBlockMetadata(pos));
	}
	
	@Override
	public boolean isAirBlock(BlockPos pos) {
		return access.isAirBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public Block getBlock(int x, int y, int z) {
		return access.getBlock(x, y, z);
	}

	@Override
	public Block getBlock(BlockPos pos) {
		return access.getBlock(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public int getBlockMetadata(BlockPos pos) {
		return access.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public int getBlockMetadata(int x, int y, int z) {
		return access.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return access.isAirBlock(x, y, z);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return new Biome(getBiomeGenForCoords(pos.getX(), pos.getZ()));
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
	public TileEntity getTileEntity(BlockPos pos) {
		return getTileEntity(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		return access.getTileEntity(x, y, z);
	}
	
	@Override
	public int isBlockProvidingPowerTo(BlockPos pos, EnumFacing side) {
		return isBlockProvidingPowerTo(pos.getX(), pos.getY(), pos.getZ(), side.getIndex());
	}

	@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
		return access.isBlockProvidingPowerTo(x, y, z, side);
	}
	
	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean def) {
		return isSideSolid(pos.getX(), pos.getY(), pos.getZ(), side.toForgeDirection(), def);
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean def) {
		return access.isSideSolid(x, y, z, side, def);
	}
	
	@Override
	public int getLightBrightnessForSkyBlocks(BlockPos pos, int minBlockBrightness) {
		return getLightBrightnessForSkyBlocks(pos.getX(), pos.getY(), pos.getZ(), minBlockBrightness);
	}

	@Override
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minBlockBrightness) {
		return access.getLightBrightnessForSkyBlocks(x, y, z, minBlockBrightness);
	}
	
}
