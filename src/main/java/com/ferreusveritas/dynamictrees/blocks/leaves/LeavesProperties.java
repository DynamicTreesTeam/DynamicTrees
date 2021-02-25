package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

/**
 * This class provides a means of holding individual properties
 * for leaves.  This is necessary since leaves can contain sub blocks
 * that may behave differently.  Each leaves properties object
 * must have a reference to a tree family.
 * 
 * @author ferreusveritas
 */
public class LeavesProperties extends ForgeRegistryEntry<LeavesProperties> {
	
	public static final LeavesProperties NULL_PROPERTIES = new LeavesProperties() {
		@Override public LeavesProperties setTree(TreeFamily tree) { return this; }
		@Override public TreeFamily getTree() { return TreeFamily.NULL_FAMILY; }
		@Override public BlockState getPrimitiveLeaves() { return Blocks.AIR.getDefaultState(); }
		@Override public ItemStack getPrimitiveLeavesItemStack() { return ItemStack.EMPTY; }
		@Override public LeavesProperties setDynamicLeavesState(BlockState state) { return this; }
		@Override public BlockState getDynamicLeavesState() { return Blocks.AIR.getDefaultState(); }
		@Override public BlockState getDynamicLeavesState(int hydro) { return Blocks.AIR.getDefaultState(); }
		@Override public ICellKit getCellKit() { return CellKits.NULLCELLKIT; }
		@Override public int getFlammability() { return 0; }
		@Override public int getFireSpreadSpeed() { return 0; }
		@Override public int getSmotherLeavesMax() { return 0; }
		@Override public int getLightRequirement() { return 15; }
		@Override public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return false; }
	};

	/** The registry. This is used for registering and querying {@link LeavesProperties} objects. */
	public static IForgeRegistry<LeavesProperties> REGISTRY;

	protected static final int maxHydro = 4;

	/** The primitive (vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	protected BlockState primitiveLeaves;

	/** The {@link ICellKit}, which is for leaves automata. */
	protected ICellKit cellKit;

	protected TreeFamily tree = TreeFamily.NULL_FAMILY;
	protected BlockState[] dynamicLeavesBlockHydroStates = new BlockState[maxHydro+1];
	protected int flammability = 60;// Mimic vanilla leaves
	protected int fireSpreadSpeed = 30;// Mimic vanilla leaves

	private LeavesProperties() {}
	
	public LeavesProperties(final BlockState primitiveLeaves, final ResourceLocation registryName) {
		this(primitiveLeaves, TreeRegistry.findCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "deciduous")), registryName);
	}
	
	public LeavesProperties(final BlockState primitiveLeaves, final ICellKit cellKit, final ResourceLocation registryName) {
		this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : DTRegistries.blockStates.AIR;
		this.cellKit = cellKit;
		this.setRegistryName(registryName);
	}

	/**
	 * Gets the primitive (vanilla) leaves for these {@link LeavesProperties}.
	 *
	 * @return The {@link BlockState} for the primitive leaves.
	 */
	public BlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}

	/**
	 * Gets {@link ItemStack} of the primitive (vanilla) leaves (for things like when it's sheared).
	 *
	 * @return The {@link ItemStack} object.
	 */
	public ItemStack getPrimitiveLeavesItemStack() {
		return new ItemStack(Item.BLOCK_TO_ITEM.get(getPrimitiveLeaves().getBlock()));
	}
	
	public LeavesProperties setDynamicLeavesState(BlockState state) {
		//Cache all the blockStates to speed up worldgen
		dynamicLeavesBlockHydroStates[0] = Blocks.AIR.getDefaultState();
		for(int i = 1; i <= maxHydro; i++) {
			dynamicLeavesBlockHydroStates[i] = state.with(DynamicLeavesBlock.DISTANCE, i);
		}
		
		return this;
	}
	
	public BlockState getDynamicLeavesState() {
		return dynamicLeavesBlockHydroStates[ maxHydro ];
	}
	
	public BlockState getDynamicLeavesState(int hydro) {
		return dynamicLeavesBlockHydroStates[MathHelper.clamp(hydro, 0, maxHydro)];
	}

	public boolean hasDynamicLeavesBlock() {
		if (getDynamicLeavesState() == null) return false;
		return getDynamicLeavesState().getBlock() instanceof DynamicLeavesBlock;
	}

	/**
	 * Sets the type of tree these leaves connect to.
	 *
	 * @param tree The {@link TreeFamily} object to set.
	 * @return This {@link LeavesProperties} object.
	 */
	public LeavesProperties setTree(TreeFamily tree) {
		this.tree = tree;
		if (tree.isFireProof()) {
			flammability = 0;
			fireSpreadSpeed = 0;
		}
		return this;
	}

	/**
	 * Used by the {@link DynamicLeavesBlock} to find out if it can pull hydro from a branch.
	 *
	 * @return The {@link TreeFamily} for these {@link LeavesProperties}.
	 * */
	public TreeFamily getTree() {
		return tree;
	}
	
	public int getFlammability() {
		return flammability;
	}
	
	public int getFireSpreadSpeed() {
		return fireSpreadSpeed;
	}

	/**
	 * Gets the smother leaves max - the maximum amount of leaves in a stack before the
	 * bottom-most leaf block dies. Can beset to zero to disable smothering. [default = 4]
	 *
	 * @return The smother leaves max.
	 */
	public int getSmotherLeavesMax() {
		return 4;
	}

	/**
	 * Gets the minimum amount of light necessary for a leaves block to be created. [default = 13]
	 *
	 * @return The minimum light requirement.
	 */
	public int getLightRequirement() {
		return 13;
	}

	/**
	 * Gets the {@link ICellKit}, which is for leaves automata.
	 *
	 * @return The {@link ICellKit} object.
	 */
	public ICellKit getCellKit() {
		return cellKit;
	}

	@OnlyIn(Dist.CLIENT)
	public int foliageColorMultiplier(BlockState state, IBlockDisplayReader world, BlockPos pos) {
		return Minecraft.getInstance().getBlockColors().getColor(getPrimitiveLeaves(), world, pos, 0);
	}

	/**
	 * Allows the leaves to perform a specific needed behavior or to optionally cancel the update
	 *
	 * @param worldIn
	 * @param pos
	 * @param state
	 * @param rand
	 * @return return true to allow the normal DynamicLeavesBlock update to occur
	 */
	public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) { return true; }

	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return fromRadius == 1 && from.getFamily().isCompatibleDynamicLeaves(blockAccess.getBlockState(pos), blockAccess, pos) ? 1 : 0;
	}

	@Override
	public String toString() {
		return "LeavesProperties{" +
				"primitiveLeaves=" + primitiveLeaves +
				", tree=" + tree + '}';
	}

}
