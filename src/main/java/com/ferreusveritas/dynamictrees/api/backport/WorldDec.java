package com.ferreusveritas.dynamictrees.api.backport;

import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**
 * World object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public class WorldDec extends BlockAccessDec {

	private final World world;
	
	public WorldDec(World world) {
		super(world);
		this.world = world;
	}

	public World getWorld() {
		return world;
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

}
