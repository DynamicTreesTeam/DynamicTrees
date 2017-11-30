package com.ferreusveritas.dynamictrees.api.backport;

import java.util.Random;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * World object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public class WorldDec extends BlockAccessDec {

	private final World world;
	public Random rand;
	
	public WorldDec(World world) {
		super(world);
		this.world = world;
		this.rand = world.rand;
	}

	public World getWorld() {
		return world;
	}
	
	public boolean isRemote() {
		return world.isRemote;
	}
	
	public boolean restoringBlockSnapshots() {
		return world.restoringBlockSnapshots;
	}
	
	public void setBlockState(BlockPos pos, IBlockState blockState, int flags) {
		world.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockState.getBlock(), blockState.getMeta(), flags);
	}

	public void setBlockState(BlockPos pos, IBlockState blockState) {
		world.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockState.getBlock(), blockState.getMeta(), 3);
	}
	
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		return world.getSavedLightValue(type, pos.getX(), pos.getY(), pos.getZ());
	}
	
	public void setBlockToAir(BlockPos pos) {
		world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public BlockPos getHeight(BlockPos pos) {
		return new BlockPos(pos.getX(), world.getHeightValue(pos.getX(), pos.getZ()), pos.getZ());
	}

	public BiomeGenBase getBiome(BlockPos pos) {
		return world.getBiomeGenForCoords(pos.getX(), pos.getZ());
	}
	
	public boolean canBlockSeeSky(BlockPos pos) {
		return world.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ());
	}
}
