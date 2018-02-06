package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LeavesProperties implements ILeavesProperties {

	private IBlockState primitiveLeaves;
	private ItemStack primitiveLeavesItemStack;
	private ICellKit cellKit;
	private DynamicTree tree = DynamicTree.NULLTREE;
	private IBlockState dynamicLeavesBlockHydroStates[] = new IBlockState[5];

	public LeavesProperties(IBlockState primitiveLeaves, ItemStack primitiveLeavesItemStack) {
		this(primitiveLeaves, primitiveLeavesItemStack, TreeRegistry.findCellKit(new ResourceLocation(ModConstants.MODID, "deciduous")));
	}
	
	public LeavesProperties(IBlockState primitiveLeaves, ItemStack primitiveLeavesItemStack, ICellKit cellKit) {
		this.primitiveLeaves = primitiveLeaves;
		this.primitiveLeavesItemStack = primitiveLeavesItemStack;
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
		for(int i = 1; i <= 4; i++) {
			dynamicLeavesBlockHydroStates[i] = state.withProperty(BlockDynamicLeaves.HYDRO, i); 
		}
		
		return this;
	}
	
	@Override
	public IBlockState getDynamicLeavesState() {
		return dynamicLeavesBlockHydroStates[5];//This is Hydro == 4
	}
	
	@Override
	public IBlockState getDynamicLeavesState(int hydro) {
		return dynamicLeavesBlockHydroStates[MathHelper.clamp(hydro, 0, 4)];
	}
	
	@Override
	public ILeavesProperties setTree(DynamicTree tree) {
		this.tree = tree;
		return this;
	}
	
	@Override
	public DynamicTree getTree() {
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
	
}
