package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;

/**
 * This class provides a means of holding individual properties
 * for leaves.  This is necessary since leaves can contain sub blocks
 * that may behave differently.  Each leaves properties object
 * must have a reference to a tree family.
 * 
 * @author ferreusveritas
 */
public class LeavesProperties implements ILeavesProperties {
	
	public static final LeavesProperties NULLPROPERTIES = new LeavesProperties() {
		@Override public ILeavesProperties setTree(TreeFamily tree) { return this; }
		@Override public TreeFamily getTree() { return TreeFamily.NULLFAMILY; }
		@Override public BlockState getPrimitiveLeaves() { return Blocks.AIR.getDefaultState(); }
		@Override public ItemStack getPrimitiveLeavesItemStack() { return ItemStack.EMPTY; }
		@Override public ILeavesProperties setDynamicLeavesState(BlockState state) { return this; }
		@Override public BlockState getDynamicLeavesState() { return Blocks.AIR.getDefaultState(); }
		@Override public BlockState getDynamicLeavesState(int hydro) { return Blocks.AIR.getDefaultState(); }
//		@Override public ICellKit getCellKit() { return CellKits.NULLCELLKIT; }
		@Override public int getFlammability() { return 0; }
		@Override public int getFireSpreadSpeed() { return 0; }
		@Override public int getSmotherLeavesMax() { return 0; }
		@Override public int getLightRequirement() { return 15; }
		@Override public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return false; }
	};
	
	protected BlockState primitiveLeaves;
	protected ItemStack primitiveLeavesItemStack;
	protected ICellKit cellKit;
	protected TreeFamily tree = TreeFamily.NULLFAMILY;
	protected BlockState dynamicLeavesBlockHydroStates[] = new BlockState[5];
	
	private LeavesProperties() {}
	
	public LeavesProperties(BlockState primitiveLeaves) {
//		this(primitiveLeaves, new ItemStack(Item.getItemFromBlock(primitiveLeaves.getBlock()), 1, primitiveLeaves.getBlock().damageDropped(primitiveLeaves)));
	}
	
	public LeavesProperties(BlockState primitiveLeaves, ICellKit cellKit) {
//		this(primitiveLeaves, new ItemStack(Item.getItemFromBlock(primitiveLeaves.getBlock()), 1, primitiveLeaves.getBlock().damageDropped(primitiveLeaves)), cellKit);
	}
	
	public LeavesProperties(BlockState primitiveLeaves, ItemStack primitiveLeavesItemStack) {
//		this(primitiveLeaves, primitiveLeavesItemStack, TreeRegistry.findCellKit(new ResourceLocation(DynamicTrees.MODID, "deciduous")));
	}
	
	public LeavesProperties(BlockState primitiveLeaves, ItemStack primitiveLeavesItemStack, ICellKit cellKit) {
//		this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : ModRegistries.blockStates.air;
		this.primitiveLeavesItemStack = primitiveLeavesItemStack != null ? primitiveLeavesItemStack : ItemStack.EMPTY;
		this.cellKit = cellKit;
	}
	
	@Override
	public BlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}
	
	@Override
	public ItemStack getPrimitiveLeavesItemStack() {
		return primitiveLeavesItemStack;
	}
	
	@Override
	public ILeavesProperties setDynamicLeavesState(BlockState state) {
		
		//Cache all the blockStates to speed up worldgen
		dynamicLeavesBlockHydroStates[0] = Blocks.AIR.getDefaultState();
		for(int i = 1; i <= 4; i++) {
//			dynamicLeavesBlockHydroStates[i] = state.with(BlockDynamicLeaves.HYDRO, i);
		}
		
		return this;
	}
	
	@Override
	public BlockState getDynamicLeavesState() {
		return dynamicLeavesBlockHydroStates[4];
	}
	
	@Override
	public BlockState getDynamicLeavesState(int hydro) {
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

	@Override
	public int foliageColorMultiplier(BlockState state, World world, BlockPos pos) {
		return 0;
	}

	//	@OnlyIn(Dist.CLIENT)
	public int foliageColorMultiplier(BlockState state, IBlockReader world, BlockPos pos) {
//		return Minecraft.getInstance().getBlockColors().colorMultiplier(getPrimitiveLeaves(), world, pos, 0);
		return 0;
	}
	
	@Override
	public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return true; }
	
	@Override
	public boolean appearanceChangesWithHydro() {
		return false;
	}
	
	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from, Direction side, int fromRadius) {
//		return fromRadius == 1 && from.getFamily().isCompatibleDynamicLeaves(blockAccess.getBlockState(pos), blockAccess, pos) ? 1 : 0;
		return 0;
	}
	
}
