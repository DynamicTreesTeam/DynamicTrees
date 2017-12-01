package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.BlockBackport;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.EnumHand;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.inspectors.NodeDisease;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruit;
import com.ferreusveritas.dynamictrees.renderers.RendererRootyDirt;
import com.ferreusveritas.dynamictrees.renderers.RendererRootyDirt.RenderType;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;

public class BlockRootyDirt extends BlockBackport implements ITreePart {

	static String name = "rootydirt";
	
	public IIcon dirtIcon;
	public IIcon grassIcon;
	public IIcon myceliumIcon;
	public IIcon podzolIcon;
	
	public BlockRootyDirt() {
		super(Material.ground);
		setStepSound(soundTypeGrass);
		setTickRandomly(true);
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	// N/A in 1.7.10
	
	
	

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		grow(world, pos, random);
	}

	public boolean grow(World world, BlockPos pos, Random random) {

		BlockBranch branch = TreeHelper.getBranch(world, pos.up());

		if(branch != null) {
			DynamicTree tree = branch.getTree();
			float growthRate = tree.getGrowthRate(world, pos.up()) * ConfigHandler.treeGrowthRateMultiplier;
			do {
				if(random.nextFloat() < growthRate) {
					int life = getSoilLife(world, pos);
					if(life > 0 && CoordUtils.isSurroundedByExistingChunks(world, pos)){
						boolean success = false;

						float energy = tree.getEnergy(world, pos.up());
						for(int i = 0; !success && i < 1 + tree.getRetries(); i++) {//Some species have multiple growth retry attempts
							success = branch.growSignal(world, pos.up(), new GrowSignal(branch, pos, energy)).success;
						}

						int soilLongevity = tree.getSoilLongevity(world, pos.up()) * (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed

						if(soilLongevity <= 0 || random.nextInt(soilLongevity) == 0) {//1 in X(soilLongevity) chance to draw nutrients from soil
							setSoilLife(world, pos, life - 1);//decrement soil life
						}
					} else {
						if(random.nextFloat() < ConfigHandler.diseaseChance && CoordUtils.isSurroundedByExistingChunks(world, pos)) {
							branch.analyse(world, pos.up(), EnumFacing.DOWN, new MapSignal(new NodeDisease(tree)));
						} else {
							NodeFruit nodeFruit = tree.getNodeFruit(world, pos.up());
							if(nodeFruit != null && CoordUtils.isSurroundedByExistingChunks(world, pos)) {
								branch.analyse(world, pos.up(), EnumFacing.DOWN, new MapSignal(nodeFruit));
							}
						}
					}
				}
			} while(--growthRate > 0.0f);
		} else {
			world.setBlockState(pos, new BlockState(Blocks.dirt));
			return false;
		}

		return true;
	}

	@Override
	public Item getItemDropped(int metadata, Random random, int fortune) {
		return Item.getItemFromBlock(Blocks.dirt);
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos) {
		return 20.0f;//Encourage proper tool usage and discourage bypassing tree felling by digging the root from under the tree
	};

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos pos, int side) {
		return getSoilLife(world, pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(heldItem != null) {//Something in the hand
			return applyItemSubstance(world, pos, player, hand, heldItem);
		}

		return false;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());

		if(branch != null && branch.getTree().applySubstance(world, pos, this, itemStack)) {
			if (itemStack.getItem() instanceof IEmptiable) {//A substance deployed from a refillable container
				if(!player.capabilities.isCreativeMode) {
					IEmptiable emptiable = (IEmptiable) itemStack.getItem();
					player.setCurrentItemOrArmor(0, emptiable.getEmptyContainer());
				}
			}
			else if(itemStack.getItem() == Items.potionitem) {//An actual potion
				if(!player.capabilities.isCreativeMode) {
					player.setCurrentItemOrArmor(0, new ItemStack(Items.glass_bottle));
				}
			} else {
				itemStack.stackSize--; //Just a regular item like bonemeal
			}
			return true;
		}
		return false;
	}

	public void destroyTree(World world, BlockPos pos) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());
		if(branch != null) {
			branch.destroyEntireTree(world, pos.up());
		}
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, int localMeta, EntityPlayer player) {
		destroyTree(world, pos);
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		destroyTree(world, pos);
	}

	public int getSoilLife(BlockAccess blockAccess, BlockPos pos) {
		return blockAccess.getBlockMetadata(pos);
	}

	public void setSoilLife(World world, BlockPos pos, int life) {
		world.real().setBlockMetadataWithNotify(pos.getX(), pos.getY(), pos.getZ(), MathHelper.clamp_int(life, 0, 15), 3);
		world.real().func_147453_f(pos.getX(), pos.getY(), pos.getZ(), this);//Notify all neighbors of NSEWUD neighbors
	}

	public boolean fertilize(World world, BlockPos pos, int amount) {
		int soilLife = getSoilLife(world, pos);
		if((soilLife == 0 && amount < 0) || (soilLife == 15 && amount > 0)) {
			return false;//Already maxed out
		}
		setSoilLife(world, pos, soilLife + amount);
		return true;
	}

	@Override
	public ICell getHydrationCell(BlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, DynamicTree leavesTree) {
		return Cells.nullCell;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(BlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		return 8;
	}

	@Override
	public int probabilityForBlock(BlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}

	@Override
	public int getRadius(BlockAccess blockAccess, BlockPos pos) {
		return 0;
	}

	@Override
	public boolean isRootNode() {
		return true;
	}

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		signal.run(world, this, pos, fromDir);//Run inspector of choice

		signal.root = pos;
		signal.found = true;

		return signal;
	}

	@Override
	public int branchSupport(BlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return dir == EnumFacing.DOWN ? 0x11 : 0;
	}

	@Override
	public DynamicTree getTree(BlockAccess blockAccess, BlockPos pos) {
		return TreeHelper.isBranch(blockAccess, pos.up()) ? TreeHelper.getSafeTreePart(blockAccess, pos.up()).getTree(blockAccess, pos.up()) : null;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if(RendererRootyDirt.renderPass == 1) {//First Pass
			switch(side) {
				case 0: return dirtIcon;//Bottom
				case 1: switch(RendererRootyDirt.renderType) {//Top
					case GRASS: return Blocks.grass.getIcon(side, 0);
					case MYCELIUM: return Blocks.mycelium.getIcon(side, 0);
					case PODZOL: return Blocks.dirt.getIcon(side, 2);
					default: return Blocks.dirt.getIcon(side, 0);
					}
				default: switch(RendererRootyDirt.renderType) {//All other sides
					case GRASS: return grassIcon;
					case MYCELIUM: return myceliumIcon;
					case PODZOL: return podzolIcon;
					default: return dirtIcon;
				}
			}
		} else {//Second Pass
			if(RendererRootyDirt.renderType == RenderType.GRASS) {
				if(side == 1) {//Top
					return Blocks.grass.getIcon(side, 0);
				} else if(side != 0) {//NSWE
					return BlockGrass.getIconSideOverlay();
				}
			}
		}

		return dirtIcon;//Everything else
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if(side == 1) {
			return Blocks.dirt.getIcon(side, 0);
		}
		return dirtIcon;
	}

	public RenderType getRenderType(BlockAccess blockAccess, int x, int y, int z) {

		final int dMap[] = {0, -1, 1};

		for(int depth = 0; depth < 3; depth++) {
			for(EnumFacing d: EnumFacing.HORIZONTALS) {
				BlockPos pos = new BlockPos(x + d.getFrontOffsetX(), y + dMap[depth], z + d.getFrontOffsetZ());
				IBlockState mimic = blockAccess.getBlockState(pos);

				if(mimic.equals(Blocks.grass)) {
					return RenderType.GRASS;
				} else if(mimic.equals(Blocks.mycelium)) {
					return RenderType.MYCELIUM;
				} else if(mimic.equals(Blocks.dirt, 2)) {
					return RenderType.PODZOL;
				}
			}
		}

		return RenderType.DIRT;//Default to plain old dirt
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {

		if(super.shouldSideBeRendered(access, x, y, z, side)) {
			if(RendererRootyDirt.renderPass == 1) {//First Pass
				if(RendererRootyDirt.renderType == RenderType.GRASS) {
					return side != 1;//Don't render top of grass block on first pass	
				}
				return true;//Render all sides of dirt, mycelium and podzol block on first pass
			} else {//Second Pass
				if(RendererRootyDirt.renderType == RenderType.GRASS) {
					return side != 0;//Don't render bottom of grass block on second pass	
				}
				return false;//Render nothing for dirt, mycelium and podzol block on second pass
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		if(RendererRootyDirt.renderType == RenderType.GRASS && RendererRootyDirt.renderPass == 2) {
			return Blocks.grass.colorMultiplier(blockAccess, x, y, z);
		} else {
			return super.colorMultiplier(blockAccess, x, y, z);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register) {
		dirtIcon = register.registerIcon(DynamicTrees.MODID + ":" + "rootydirt-dirt");
		grassIcon = register.registerIcon(DynamicTrees.MODID + ":" + "rootydirt-grass");
		myceliumIcon = register.registerIcon(DynamicTrees.MODID + ":" + "rootydirt-mycelium");
		podzolIcon = register.registerIcon(DynamicTrees.MODID + ":" + "rootydirt-podzol");
	}

	@Override
	public int getRenderType() {
		return RendererRootyDirt.renderId;
	}

}
