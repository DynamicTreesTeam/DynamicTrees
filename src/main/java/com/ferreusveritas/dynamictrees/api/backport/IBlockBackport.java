package com.ferreusveritas.dynamictrees.api.backport;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;

/**
 * Adapter to make a block work more like 1.10.2+
 * 
 * @author ferreusveritas
 *
 */
public interface IBlockBackport extends IRegisterable {

	public void setDefaultState(IBlockState blockState);
	
	public IBlockState getDefaultState();
	
	//public void updateTick(net.minecraft.world.World _world, int x, int y, int z, Random rand);
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand);
	
	//public boolean onBlockActivated(net.minecraft.world.World _world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ);
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ);
	
	//public float getBlockHardness(net.minecraft.world.World world, int x, int y, int z);
	public float getBlockHardness(World world, BlockPos pos);

	//public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face);
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face);

	//public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face);
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face);
	
	//public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity);
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity);
	
	//public void onBlockHarvested(net.minecraft.world.World world, int x, int y, int z, int localMeta, EntityPlayer player);
	public void onBlockHarvested(World world, BlockPos pos, int localMeta, EntityPlayer player);
	
	//public void onBlockExploded(net.minecraft.world.World world, int x, int y, int z, Explosion explosion);
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion);
	
	//public boolean isWood(IBlockAccess world, int x, int y, int z);
	public boolean isWood(IBlockAccess world, BlockPos pos);
	
	//public int getComparatorInputOverride(net.minecraft.world.World world, int x, int y, int z,	int side);
	public int getComparatorInputOverride(World world, BlockPos pos, int side);
	
	//public void onNeighborBlockChange(net.minecraft.world.World _world, int x, int y, int z, Block block);
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block);

	//public boolean canBlockStay(net.minecraft.world.World _world, int x, int y, int z);
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state);
	
	//public AxisAlignedBB getCollisionBoundingBoxFromPool(net.minecraft.world.World world, int x, int y, int z);
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, BlockPos pos);
	
	//public boolean removedByPlayer(net.minecraft.world.World world, EntityPlayer player, int x, int y, int z, boolean canHarvest);
	public boolean removedByPlayer(World world, EntityPlayer player, BlockPos pos, boolean canHarvest);
	
	//public ArrayList<ItemStack> getDrops(net.minecraft.world.World world, int x, int y, int z, int metadata, int fortune);
	public ArrayList<ItemStack> getDrops(World world, BlockPos pos, int metadata, int fortune);
	
	//protected void dropBlockAsItem(net.minecraft.world.World world, int x, int y, int z, ItemStack stack);
	public void dropBlockAsItem(World world, BlockPos pos, ItemStack stack);
	
    public void dropBlockAsItem(World world, BlockPos pos, int meta, int fortune);
	
}
