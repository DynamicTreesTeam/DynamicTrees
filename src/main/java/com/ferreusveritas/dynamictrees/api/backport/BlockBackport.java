package com.ferreusveritas.dynamictrees.api.backport;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.util.IRegisterable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Adapter to make a block work more like 1.10.2+
 * 
 * @author ferreusveritas
 *
 */
public class BlockBackport extends Block implements IRegisterable {

	public IBlockState defBlockState;
	public static final AxisAlignedBB FULL_AABB = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
	
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
	public void updateTick(net.minecraft.world.World _world, int x, int y, int z, Random rand) {
		World world = new World(_world);
		BlockPos pos = new BlockPos(x, y, z);
		this.updateTick(world, pos, world.getBlockState(pos), rand);
	}
	
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world.getWorld(), pos.getX(), pos.getY(),  pos.getZ(), rand);
	}
	
	@Override
	public boolean onBlockActivated(net.minecraft.world.World _world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
		World world = new World(_world);
		BlockPos pos = new BlockPos(x, y, z);
		return this.onBlockActivated(world, pos, world.getBlockState(pos), player, EnumHand.MAIN_HAND, player.getHeldItem(), EnumFacing.getFront(facing), hitX,	hitY, hitZ);
	}
	
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return super.onBlockActivated(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), player, side.getIndex(), hitX, hitY, hitZ);
	}
	
	@Override
	public float getBlockHardness(net.minecraft.world.World world, int x, int y, int z) {
		return this.getBlockHardness(new World(world), new BlockPos(x, y, z));
	}
	
	public float getBlockHardness(World world, BlockPos pos) {
		return super.getBlockHardness(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
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
	public void onBlockHarvested(net.minecraft.world.World world, int x, int y, int z, int localMeta, EntityPlayer player) {
		this.onBlockHarvested(new World(world), new BlockPos(x, y, z), localMeta, player);
	}
	
	public void onBlockHarvested(World world, BlockPos pos, int localMeta, EntityPlayer player) {
		super.onBlockHarvested(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), localMeta, player);
	}
	
	@Override
	public void onBlockExploded(net.minecraft.world.World world, int x, int y, int z, Explosion explosion) {
		this.onBlockExploded(new World(world), new BlockPos(x, y, z), explosion);
	}
	
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		super.onBlockExploded(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), explosion);
	}
	
	@Override
	public boolean isWood(IBlockAccess world, int x, int y, int z) {
		return this.isWood(world, new BlockPos(x, y, z));
	}
	
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		return super.isWood(world, pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public int getComparatorInputOverride(net.minecraft.world.World world, int x, int y, int z,	int side) {
		return this.getComparatorInputOverride(new World(world), new BlockPos(x, y, z), side);
	}
	
	public int getComparatorInputOverride(World world, BlockPos pos, int side) {
		return super.getComparatorInputOverride(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), side);
	}
	
	@Override
	public void onNeighborBlockChange(net.minecraft.world.World _world, int x, int y, int z, Block block) {
		World world = new World(_world);
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos); 
		this.neighborChanged(state, world, pos, block);
	}
	
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		super.onNeighborBlockChange(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), block);
	}

	@Override
	public boolean canBlockStay(net.minecraft.world.World _world, int x, int y, int z) {
		World world = new World(_world);
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		return this.canBlockStay(world, pos, state);
	}
	
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return super.canBlockStay(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(net.minecraft.world.World world, int x, int y, int z) {
		return this.getCollisionBoundingBoxFromPool(new World(world), new BlockPos(x, y, z));
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, BlockPos pos) {
		return super.getCollisionBoundingBoxFromPool(world.getWorld(), pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	public boolean removedByPlayer(net.minecraft.world.World world, EntityPlayer player, int x, int y, int z, boolean canHarvest) {
		return this.removedByPlayer(new World(world), player, new BlockPos(x, y, z), canHarvest);
	}
	
	public boolean removedByPlayer(World world, EntityPlayer player, BlockPos pos, boolean canHarvest) {
		return super.removedByPlayer(world.getWorld(), player, pos.getX(), pos.getY(), pos.getZ(), canHarvest);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(net.minecraft.world.World world, int x, int y, int z, int metadata, int fortune) {
		return this.getDrops(new World(world), new BlockPos(x, y, z), metadata, fortune);
	}

	public ArrayList<ItemStack> getDrops(World world, BlockPos pos, int metadata, int fortune) {
		return super.getDrops(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), metadata, fortune);
	}
	
	@Override
	protected void dropBlockAsItem(net.minecraft.world.World world, int x, int y, int z, ItemStack stack) {
		this.dropBlockAsItem(new World(world), new BlockPos(x, y, z), stack);
	}
	
	protected void dropBlockAsItem(World world, BlockPos pos, ItemStack stack) {
		super.dropBlockAsItem(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), stack);
	}
	
    public final void dropBlockAsItem(World world, BlockPos pos, int meta, int fortune) {
    	super.dropBlockAsItem(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
    }
	
	/*
	@Override
	public void setBlockBoundsForItemRender() {
		AxisAlignedBB aabb = getBoundingBox(null, null, null);
		this.setBlockBounds((float)aabb.minX, (float)aabb.minY, (float)aabb.minZ, (float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return FULL_AABB;
	}*/
	
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
