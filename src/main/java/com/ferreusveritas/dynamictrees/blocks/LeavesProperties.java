package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * This class provides a means of holding individual properties for leaves.  This is necessary since leaves can contain
 * sub blocks that may behave differently.  Each leaves properties object must have a reference to a tree family.
 *
 * @author ferreusveritas
 */
public class LeavesProperties implements ILeavesProperties {

	public static final LeavesProperties NULLPROPERTIES = new LeavesProperties() {
		@Override
		public ILeavesProperties setTree(TreeFamily tree) {
			return this;
		}

		@Override
		public TreeFamily getTree() {
			return TreeFamily.NULLFAMILY;
		}

		@Override
		public IBlockState getPrimitiveLeaves() {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public ItemStack getPrimitiveLeavesItemStack() {
			return ItemStack.EMPTY;
		}

		@Override
		public ILeavesProperties setDynamicLeavesState(IBlockState state) {
			return this;
		}

		@Override
		public IBlockState getDynamicLeavesState() {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public IBlockState getDynamicLeavesState(int hydro) {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public ICellKit getCellKit() {
			return CellKits.NULLCELLKIT;
		}

		@Override
		public int getFlammability() {
			return 0;
		}

		@Override
		public int getFireSpreadSpeed() {
			return 0;
		}

		@Override
		public int getSmotherLeavesMax() {
			return 0;
		}

		@Override
		public int getLightRequirement() {
			return 15;
		}

		@Override
		public boolean updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
			return false;
		}
	};

	protected IBlockState primitiveLeaves;
	protected ItemStack primitiveLeavesItemStack;
	protected ICellKit cellKit;
	protected TreeFamily tree = TreeFamily.NULLFAMILY;
	protected IBlockState[] dynamicLeavesBlockHydroStates = new IBlockState[5];

	private LeavesProperties() {
	}

	public LeavesProperties(IBlockState primitiveLeaves) {
		this(primitiveLeaves, new ItemStack(Item.getItemFromBlock(primitiveLeaves.getBlock()), 1, primitiveLeaves.getBlock().damageDropped(primitiveLeaves)));
	}

	public LeavesProperties(IBlockState primitiveLeaves, ICellKit cellKit) {
		this(primitiveLeaves, new ItemStack(Item.getItemFromBlock(primitiveLeaves.getBlock()), 1, primitiveLeaves.getBlock().damageDropped(primitiveLeaves)), cellKit);
	}

	public LeavesProperties(IBlockState primitiveLeaves, ItemStack primitiveLeavesItemStack) {
		this(primitiveLeaves, primitiveLeavesItemStack, TreeRegistry.findCellKit(new ResourceLocation(ModConstants.MODID, "deciduous")));
	}

	public LeavesProperties(IBlockState primitiveLeaves, ItemStack primitiveLeavesItemStack, ICellKit cellKit) {
		this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : ModBlocks.blockStates.air;
		this.primitiveLeavesItemStack = primitiveLeavesItemStack != null ? primitiveLeavesItemStack : ItemStack.EMPTY;
		this.cellKit = cellKit;
	}

	@Override
	public IBlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}

	@Override
	public ItemStack getPrimitiveLeavesItemStack() {
		return primitiveLeavesItemStack;
	}

	@Override
	public ILeavesProperties setDynamicLeavesState(IBlockState state) {

		//Cache all the blockStates to speed up worldgen
		dynamicLeavesBlockHydroStates[0] = Blocks.AIR.getDefaultState();
		for (int i = 1; i <= 4; i++) {
			dynamicLeavesBlockHydroStates[i] = state.withProperty(BlockDynamicLeaves.HYDRO, i);
		}

		return this;
	}

	@Override
	public IBlockState getDynamicLeavesState() {
		return dynamicLeavesBlockHydroStates[4];
	}

	@Override
	public IBlockState getDynamicLeavesState(int hydro) {
		return dynamicLeavesBlockHydroStates[MathHelper.clamp(hydro, 0, 4)];
	}

	@Override
	public ILeavesProperties setTree(TreeFamily tree) {
		this.tree = tree;
		return this;
	}

	@Override
	public TreeFamily getTree() {
		return tree;
	}

	@Override
	public int getFlammability() {
		return 60;// Mimic vanilla leaves
	}

	@Override
	public int getFireSpreadSpeed() {
		return 30;// Mimic vanilla leaves
	}

	@Override
	public int getSmotherLeavesMax() {
		return 4;
	}

	@Override
	public int getLightRequirement() {
		return 13;
	}

	@Override
	public ICellKit getCellKit() {
		return cellKit;
	}

	@SideOnly(Side.CLIENT)
	public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
		return Minecraft.getMinecraft().getBlockColors().colorMultiplier(getPrimitiveLeaves(), world, pos, 0);
	}

	@Override
	public boolean updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		return true;
	}

	@Override
	public boolean appearanceChangesWithHydro() {
		return false;
	}

	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return fromRadius == 1 && from.getFamily().isCompatibleDynamicLeaves(blockAccess.getBlockState(pos), blockAccess, pos) ? 1 : 0;
	}

}
