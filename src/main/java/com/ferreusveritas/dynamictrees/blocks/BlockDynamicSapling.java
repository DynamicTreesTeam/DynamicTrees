package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.Dir;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDynamicSapling extends BlockBackport {

	protected Map<Integer, DynamicTree> trees = new HashMap<Integer, DynamicTree>();
	
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
	public void updateTick(World world, int x, int y, int z, Random rand) {
		generateTree(world, new BlockPos(x, y, z), rand);
	}

	public static boolean canSaplingStay(IBlockAccess blockAccess, DynamicTree tree, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for(ForgeDirection dir: Dir.HORIZONTALS) {
			Block block = pos.offset(dir).getBlock(blockAccess);
			if(TreeHelper.isBranch(block) || block instanceof BlockDynamicSapling) {
				return false;
			}
		}

		//Air above and acceptable soil below
		return pos.up().isAirBlock(blockAccess) && tree.isAcceptableSoil(pos.down().getBlockState(blockAccess));
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		return canSaplingStay(world, getTree(world, pos), pos);
	}

	public void generateTree(World world, BlockPos pos, Random rand) {
		DynamicTree tree = getTree(world, pos);
		if(canBlockStay(world, pos.getX(), pos.getY(), pos.getZ())) {
			//Ensure planting conditions are right
			world.setBlock(pos.getX(), pos.getY() - 1, pos.getZ(), tree.getRootyDirtBlock(), 15, 3);//Set to fully fertilized rooty dirt
			world.setBlock(pos.getX(), pos.getY(), pos.getZ(), tree.getGrowingBranch(), 0, 3);//Set to a single branch with 1 radius
			tree.getGrowingLeaves().growLeaves(world, tree, pos.up());//Make a single block of leaves above the trunk
		} else {
			dropBlock(world, tree, pos);
		}
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public DynamicTree getTree(int metadata) {
		return trees.get(metadata);
	}

	public DynamicTree getTree(IBlockAccess world, BlockPos pos) {
		return getTree(pos.getMeta(world));
	}

	public BlockDynamicSapling setTree(int metadata, DynamicTree tree) {
		trees.put(metadata, tree);
		return this;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, Block block) {
		if (!this.canBlockStay(world, pos.getX(), pos.getY(), pos.getZ())) {
			dropBlock(world, getTree(world, pos), pos);
		}
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////

	private void dropBlock(World world, DynamicTree tree, BlockPos pos) {
		world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
		dropBlockAsItem(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(tree.getSeed()));
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> dropped = super.getDrops(world, x, y, z, metadata, fortune);
		dropped.add(new ItemStack(getTree(metadata).getSeed()));
		return dropped;
	}

	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		return getTree(world, new BlockPos(x, y, z)).getSeed();
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return null;
	}

	public int getDamageValue(World world, int x, int y, int z) {
		return 0;
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
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
		return getTree(metadata).getPrimitiveLog().getIcon(2);//0:Ring, 2:Bark
	}
	
	@Override
	public int getRenderType() {
		return RendererSapling.id;
	}

}
