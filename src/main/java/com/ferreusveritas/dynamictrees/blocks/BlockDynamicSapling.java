package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.renderers.RendererSapling;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;

public class BlockDynamicSapling extends BlockBackport implements IGrowable {
	
	public Species tree;
	
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
		grow(world, rand, pos, state);
	}
	
	public static boolean canSaplingStay(World world, Species species, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			IBlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}
		
		//Air above and acceptable soil below
		return world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()));
	}
	
	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return canSaplingStay(world, getSpecies(state), pos);
	}

	@Override //grow(World world, Random rand, int x, int y, int z)
	public void func_149853_b(net.minecraft.world.World vworld, Random rand, int x, int y, int z) {
		World world = new World(vworld);
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		grow(world, rand, pos, state);
	}
	
	public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
		Species species = getSpecies(state);
		if(canBlockStay(world, pos, state)) {
			//Ensure planting conditions are right
			DynamicTree tree = species.getTree();
			if(world.isAirBlock(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
				world.setBlockState(pos, tree.getDynamicBranch().getDefaultState());//set to a single branch with 1 radius
				world.setBlockState(pos.up(), tree.getDynamicLeavesState());//Place a single leaf block on top
				world.setBlockState(pos.down(), species.getRootyDirtBlock().getDefaultState());//Set to fully fertilized rooty dirt underneath
			}
		} else {
			dropBlock(world, species, state, pos);
		}
	}
	
	@Override//canGrow
	public boolean func_149851_a(net.minecraft.world.World vworld, int x, int y, int z, boolean isClient) {
		World world = new World(vworld);
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		return getSpecies(state).canGrowWithBoneMeal(world, pos);
	}
	
	@Override//canUseBonemeal
	public boolean func_149852_a(net.minecraft.world.World vworld, Random rand, int x, int y, int z) {
		World world = new World(vworld);
		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		return getSpecies(state).canUseBoneMealNow(world, rand, pos);
    }
	
    
	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////
	
	public Species getSpecies(IBlockState state) {
		return this.tree;
	}
	
	public BlockDynamicSapling setSpecies(IBlockState state, Species species) {
		this.tree = species;
		return this;
	}
	
	
	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		if (!this.canBlockStay(world, pos, state)) {
			dropBlock(world, getSpecies(state), state, pos);
		}
	}
	
	private void dropBlock(World world, Species tree, IBlockState state, BlockPos pos) {
		world.setBlockToAir(pos);
		dropBlockAsItem(world.real(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(tree.getSeed()));
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> dropped = super.getDrops(world, pos, state, fortune);
		dropped.add(getSpecies(state).getSeedStack(1));
		return dropped;
	}
	
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
	}
	
	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	public Item getItem(net.minecraft.world.World _world, int x, int y, int z) {
		World world = new World(_world);
		return getSpecies(world.getBlockState(new BlockPos(x, y, z))).getSeed();
	}
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	@Override
	public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess blockAccess, int x, int y, int z) {
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
		return getSpecies(new BlockState(this, metadata)).getTree().getPrimitiveLog().getIcon(2);//0:Ring, 2:Bark
	}
	
	@Override
	public int getRenderType() {
		return RendererSapling.id;
	}
	
}
