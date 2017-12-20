package com.ferreusveritas.dynamictrees.api.backport;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumSkyBlock;

/**
 * World object expansion for backport using a simple decorator pattern
 * 
 * @author ferreusveritas
 *
 */
public class World extends BlockAccess {

	private final net.minecraft.world.World world;
	public Random rand;
	
	public World(net.minecraft.world.World world) {
		super(world);
		this.world = world;
		this.rand = world.rand;
	}

	public net.minecraft.world.World real() {
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
	
	public void setBlockState(BlockPos pos, Block block) {
		world.setBlock(pos.getX(), pos.getY(), pos.getZ(), block, 0, 3);
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

	@Override
    public Biome getBiome(BlockPos pos) {
    	return new Biome(world.getBiomeGenForCoords(pos.getX(), pos.getZ()));
    }
	
	public boolean canBlockSeeSky(BlockPos pos) {
		return world.canBlockSeeTheSky(pos.getX(), pos.getY(), pos.getZ());
	}
	
    public boolean canMineBlock(EntityPlayer player, int x, int y, int z) {
    	return world.canMineBlock(player, x, y, z);
	}
    
    public boolean canMineBlock(EntityPlayer player, BlockPos pos) {
    	return canMineBlock(player, pos.getX(), pos.getY(), pos.getZ());
    }
    
	public void playSoundEffect(BlockPos pos, String soundevent, float a, float b) {
		world.playSoundEffect(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, soundevent, a, b);
	}
    
	public void playSoundEffect(double x, double y, double z, String soundevent, float a, float b) {
		world.playSoundEffect(x, y, z, soundevent, a, b);
	}
	
    public boolean isBlockIndirectlyGettingPowered(BlockPos pos) {
		return world.isBlockIndirectlyGettingPowered(pos.getX(), pos.getY(), pos.getZ());
	}
	
    public boolean isBlockIndirectlyGettingPowered(int x, int y, int z) {
		return world.isBlockIndirectlyGettingPowered(x, y, z);
	}
	
	public static boolean doesBlockHaveSolidTopSurface(World world, BlockPos pos) {
		return net.minecraft.world.World.doesBlockHaveSolidTopSurface(world.real(), pos.getX(), pos.getY(), pos.getZ());
	}

	public long getTotalWorldTime() {
		return world.getTotalWorldTime();
	}
	
}
