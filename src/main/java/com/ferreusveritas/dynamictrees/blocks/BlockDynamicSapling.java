package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockAccessDec;
import com.ferreusveritas.dynamictrees.api.backport.BlockAndMeta;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.renderers.RendererSapling;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockDynamicSapling extends BlockBackport {
	
	public DynamicTree tree;
	
	public BlockDynamicSapling(String name) {
		super(Material.plants);
		setStepSound(soundTypeGrass);
		setTickRandomly(true);
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		generateTree(world, pos, state, rand);
	}

	public static boolean canSaplingStay(BlockAccessDec access, DynamicTree tree, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			Block block = access.getBlock(pos.offset(dir));
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}

		//Air above and acceptable soil below
		return access.isAirBlock(pos) && tree.isAcceptableSoil(access.getBlockState(pos.down()));
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return canSaplingStay(world, getTree(state), pos);
	}

	public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
		DynamicTree tree = getTree(state);
		if(canBlockStay(world, pos, state)) {
			//Ensure planting conditions are right
			if(world.isAirBlock(pos.up()) && tree.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos.down(), new BlockAndMeta(tree.getRootyDirtBlock(), 15), 3);//Set to fully fertilized rooty dirt
				world.setBlockState(pos, new BlockAndMeta(tree.getDynamicBranch(), 0), 3);//Set to a single branch with 1 radius
				tree.getDynamicLeaves().growLeaves(world, tree, pos.up());//Make a single block of leaves above the trunk
			}
		} else {
			dropBlock(world, tree, state, pos);
		}
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public DynamicTree getTree(IBlockState state) {
		return this.tree;
	}

	public BlockDynamicSapling setTree(IBlockState state, DynamicTree tree) {
		this.tree = tree;
		return this;
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
		if (!this.canBlockStay(world, pos, state)) {
			dropBlock(world, getTree(state), state, pos);
		}
	}
	
	private void dropBlock(World world, DynamicTree tree, IBlockState state, BlockPos pos) {
		world.setBlockToAir(pos);
		dropBlockAsItem(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(tree.getSeed()));
	}

	@Override
	public ArrayList<ItemStack> getDrops(net.minecraft.world.World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> dropped = super.getDrops(world, x, y, z, metadata, fortune);
		dropped.add(getTree(new BlockAndMeta(this, metadata)).getSeedStack());
		return dropped;
	}

	@SideOnly(Side.CLIENT)
	public Item getItem(net.minecraft.world.World _world, int x, int y, int z) {
		World world = new World(_world);
		return getTree(world.getBlockState(new BlockPos(x, y, z))).getSeed();
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}

	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(net.minecraft.world.World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	//Bark or wood Ring texture for branches
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return getTree(new BlockAndMeta(this, metadata)).getPrimitiveLog().getIcon(2);//0:Ring, 2:Bark
	}
	
	@Override
	public int getRenderType() {
		return RendererSapling.id;
	}

}
