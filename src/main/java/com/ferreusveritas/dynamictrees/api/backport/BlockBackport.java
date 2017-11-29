package com.ferreusveritas.dynamictrees.api.backport;

import java.util.Random;

import com.ferreusveritas.dynamictrees.util.IRegisterable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Adapter to make a block work more like 1.10.2+
 * 
 * @author ferreusveritas
 *
 */
public class BlockBackport extends Block implements IRegisterable {

	public IBlockState defBlockState;
	
	protected BlockBackport(Material material) {
		super(material);
	}

	public void setDefaultState(IBlockState blockState) {
		this.defBlockState = blockState;
	}
	
	public IBlockState getDefaultState() {
		return defBlockState;
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		BlockPos pos = new BlockPos(x, y, z);
		this.updateTick(world, pos, pos.getBlockState(world), rand);
	}
	
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos.getX(), pos.getY(),  pos.getZ(), rand);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
		BlockPos pos = new BlockPos(x, y, z);
		return this.onBlockActivated(world, pos, pos.getBlockState(world), player, facing, hitX,	hitY, hitZ);
	}
	
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
		return super.onBlockActivated(world, pos.getX(), pos.getY(), pos.getZ(), player, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return this.getBlockHardness(world, new BlockPos(x, y, z));
	}
	
	public float getBlockHardness(World world, BlockPos pos) {
		return super.getBlockHardness(world, pos.getX(), pos.getY(), pos.getZ());
	};

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return this.getFlammability(world, new BlockPos(x, y, z), EnumFacing.fromForgeDirection(face));
	}
	
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return super.getFlammability(world, pos.getX(), pos.getY(), pos.getZ(), face.toForgeDirection());
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		return this.getFireSpreadSpeed(world, new BlockPos(x, y, z), EnumFacing.fromForgeDirection(face));
	}
	
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return super.getFireSpreadSpeed(world, pos.getX(), pos.getY(), pos.getZ(), face.toForgeDirection());
	}
	
	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return this.isLadder(world, new BlockPos(x, y, z), entity);
	}
	
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return super.isLadder(world, pos.getX(), pos.getY(), pos.getZ(), entity);
	}
	
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int localMeta, EntityPlayer player) {
		this.onBlockHarvested(world, new BlockPos(x, y, z), localMeta, player);
	}
	
	public void onBlockHarvested(World world, BlockPos pos, int localMeta, EntityPlayer player) {
		super.onBlockHarvested(world, pos.getX(), pos.getY(), pos.getZ(), localMeta, player);
	}
	
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		this.onBlockExploded(world, new BlockPos(x, y, z), explosion);
	}
	
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		super.onBlockExploded(world, pos.getX(), pos.getY(), pos.getZ(), explosion);
	}
	
	@Override
	public boolean isWood(IBlockAccess world, int x, int y, int z) {
		return this.isWood(world, new BlockPos(x, y, z));
	}
	
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return super.isWood(world, pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z,	int side) {
		return this.getComparatorInputOverride(world, new BlockPos(x, y, z), side);
	}
	
	public int getComparatorInputOverride(World world, BlockPos pos, int side) {
		return super.getComparatorInputOverride(world, pos.getX(), pos.getY(), pos.getZ(), side);
	}
	
	public void onNeighborBlockChange(World world, int x, int y, int z,	Block block) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = pos.getBlockState(world); 
		this.neighborChanged(state, world, pos, block);
	}
	
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		super.onNeighborBlockChange(world, pos.getX(), pos.getY(), pos.getZ(), block);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = pos.getBlockState(world);
		return this.canBlockStay(world, pos, state);
	}
	
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return super.canBlockStay(world, pos.getX(), pos.getY(), pos.getZ());
	}
		
	//////////////////////////////
	// REGISTRATION
	//////////////////////////////

	String registryName;
	
	@Override
	public void setRegistryName(String regName) {
		registryName = regName;
	}

	@Override
	public String getRegistryName() {
		return registryName;
	}

	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setBlockName(unlocalName);
	}
	
}
