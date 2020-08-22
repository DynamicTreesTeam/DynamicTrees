package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.*;
import com.ferreusveritas.dynamictrees.cells.CellMetadata;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This structure describes a Tree Family whose member Species all have a common wood type.
 *
* A {@link TreeFamily} is more or less just a definition of {@link BlockBranch} blocks.
* It also defines the cellular automata function of the {@link BlockBranch}.  It defines the type of wood that
* the tree is made of and consequently what kind of log you get when you cut it down.
*
* A DynamicTree does not contain a reference to a Seed, Leaves, Sapling, or how it should grow(how fast, how tall, etc).
* It does not control what drops it produces or what fruit it grows.  It does not control where it should grow.
* All of these capabilities lie in the Species class for which a DynamicTree should always contain one default
* species(the common species).
*
* @author ferreusveritas
*/
public class TreeFamily {
	
	public final static TreeFamily NULLFAMILY = new TreeFamily() {
		@Override public void setCommonSpecies(Species species) {}
		@Override public Species getCommonSpecies() { return Species.NULLSPECIES; }
		@Override public List<Block> getRegisterableBlocks(List<Block> blockList) { return blockList; }
		@Override public List<Item> getRegisterableItems(List<Item> itemList) { return itemList; }
		@Override public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) { return false; }
		@Override public ItemStack getStick(int qty) { return ItemStack.EMPTY; }
	};
	
	/** Simple name of the tree e.g. "oak" */
	private final ResourceLocation name;

	@Nonnull
	protected Species commonSpecies = Species.NULLSPECIES;

	//Branches
	/** The dynamic branch used by this tree family */
	private BlockBranch dynamicBranch;
	/** The primitive(vanilla) log to base the texture, drops, and other behavior from */
	private BlockState primitiveLog = Blocks.AIR.getDefaultState();
	/** cached ItemStack of primitive logs(what is returned when wood is harvested) */
	private ItemStack primitiveLogItemStack = ItemStack.EMPTY;

	//Leaves
	/** Used to modify the getRadiusForCellKit call to create a special case */
	protected boolean hasConiferVariants = false;

	//Misc
	/** The stick that is returned when a whole log can't be dropped */
	private ItemStack stick;
	/** Weather the branch can support cocoa pods on it's surface [default = false] */
	public boolean canSupportCocoa = false;

	@OnlyIn(Dist.CLIENT)
	public int woodColor;//For roots

	public TreeFamily() {
		this.name = new ResourceLocation(DynamicTrees.MODID, "null");
	}

	/**
	 * Constructor suitable for derivative mods
	 *
	 * @param name The ResourceLocation of the tree e.g. "mymod:poplar"
	 */
	public TreeFamily(ResourceLocation name) {
		this.name = name;

		setDynamicBranch(createBranch());
		stick = new ItemStack(Items.STICK);
		createSpecies();
	}

	public void createSpecies() {}

	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		speciesRegistry.register(getCommonSpecies());
	}

	public void setCommonSpecies(@Nonnull Species species) {
		commonSpecies = species;
	}

	public Species getCommonSpecies() {
		return commonSpecies;
	}

	///////////////////////////////////////////
	// SPECIES LOCATION OVERRIDES
	///////////////////////////////////////////

	/**
	 * This is only used by Rooty Dirt to get the appropriate species for this tree.
	 * For instance Oak may use this to select a Swamp Oak species if the coordinates
	 * are in a swamp.
	 *
	 * @param access
	 * @param trunkPos
	 * @return
	 */
	public Species getSpeciesForLocation(World access, BlockPos trunkPos) {
		for(ISpeciesLocationOverride override : speciesLocationOverrides) {
			Species species = override.getSpeciesForLocation(access, trunkPos);
			if(species.isValid()) {
				return species;
			}
		}
		return getCommonSpecies();
	}

	public void addSpeciesLocationOverride(ISpeciesLocationOverride override) {
		speciesLocationOverrides.add(override);
	}

	private LinkedList<ISpeciesLocationOverride> speciesLocationOverrides = new LinkedList<>();

	public interface ISpeciesLocationOverride {
		Species getSpeciesForLocation(World access, BlockPos trunkPos);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) {

		BlockPos rootPos = TreeHelper.findRootNode(state, world, hitPos);

		if(rootPos != BlockPos.ZERO) {
			TreeHelper.getExactSpecies(state, world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, hit);
		}

		return false;
	}


	///////////////////////////////////////////
	// REGISTRATION
	///////////////////////////////////////////

	/**
	 * Used to register the blocks this tree uses.  Mainly just the {@link BlockBranch}
	 * We intentionally leave out leaves since they are shared between trees
	 * */
	public List<Block> getRegisterableBlocks(List<Block> blockList) {
		if(isThick()) {
			BlockBranchThick branch = (BlockBranchThick) getDynamicBranch();
			blockList.add(branch.getPairSide(false));
			blockList.add(branch.getPairSide(true));
		} else {
			blockList.add(getDynamicBranch());
		}
		return blockList;
	}

	/**
	 * Used to register items the tree creates. Mostly for the {@link Seed}
	 * If the developer provides the seed externally instead of having it
	 * generated internally then the seed should be allowed to register here.
	 * If this can't be the case then override this member function with a
	 * dummy one.
	 */
	public List<Item> getRegisterableItems(List<Item> itemList) {
		//Register an itemBlock for the branch block
		Block branch = getDynamicBranch();
		itemList.add(new BlockItem(branch, new Item.Properties()).setRegistryName(Objects.requireNonNull(branch.getRegistryName())));

		if(isThick()) {
			//An ItemBlock must be registered in order for Waila to work properly
			branch = ((BlockBranchThick) branch).getPairSide(true);
			itemList.add(new BlockItem(branch, new Item.Properties()).setRegistryName(Objects.requireNonNull(branch.getRegistryName())));
		}

		getCommonSpecies().getSeed().ifValid(itemList::add);

		return itemList;
	}


	///////////////////////////////////////////
	// TREE PROPERTIES
	///////////////////////////////////////////

	public ResourceLocation getName() {
		return name;
	}

	public boolean isWood() {
		return true;
	}

	/**
	 * Override this to use a custom branch for the tree family
	 *
	 * @return the branch to be created
	 */
	public BlockBranch createBranch() {
		String branchName = name + "branch";
		return isThick() ? new BlockBranchThick(branchName) : new BlockBranchBasic(branchName);
	}

	protected TreeFamily setDynamicBranch(BlockBranch gBranch) {
		dynamicBranch = gBranch;//Link the tree to the branch
		dynamicBranch.setFamily(this);//Link the branch back to the tree

		return this;
	}

	public BlockBranch getDynamicBranch() {
		return dynamicBranch;
	}

	public boolean isThick() {
		return false;
	}

	public boolean autoCreateBranch() {
		return false;
	}

//	@OnlyIn(Dist.CLIENT)
	public int getWoodColor() {
		return woodColor;
	}

//	@OnlyIn(Dist.CLIENT)
	public int getRootColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
		return getWoodColor();
	}

	/**
	 * Used to set the type of stick that a tree drops when there's not enough wood volume for a log.
	 *
	 * @param itemStack An itemstack of the stick
	 * @return TreeFamily for chaining calls
	 */
	protected TreeFamily setStick(ItemStack itemStack) {
		stick = itemStack;
		return this;
	}

	/**
	 * Get a quantity of whatever is considered a stick for this tree's type of wood.
	 *
	 * @param qty Number of sticks
	 * @return an {@link ItemStack} of sticky things
	 */
	public ItemStack getStick(int qty) {
		ItemStack stack = stick.copy();
		stack.setCount(MathHelper.clamp(qty, 0, 64));
		return stack;
	}

	/**
	 * Used to set the type of log item that a tree drops when it's harvested.
	 * Uses damageDropped() to automatically set the ItemStack metadata from a BlockState.
	 *
	 * @param primLog A blockstate of the log
	 * @return TreeFamily for chaining calls
	 */
	protected TreeFamily setPrimitiveLog(BlockState primLog) {
		return setPrimitiveLog(primLog, new ItemStack(Item.getItemFromBlock(primLog.getBlock())));
	}

	/**
	 * Used to set the type of log item that a tree drops when it's harvested.
	 * Use this function to explicitly set the itemstack instead of having it
	 * done automatically.
	 *
	 * @param primLog A blockstate of the log
	 * @param primLogStack An itemStack of the log item
	 * @return TreeFamily for chaining calls
	 */
	protected TreeFamily setPrimitiveLog(BlockState primLog, ItemStack primLogStack) {
		primitiveLog = primLog;
		primitiveLogItemStack = primLogStack;
		return this;
	}
	protected TreeFamily setPrimitiveLog(Block primLogBlock) {
		return this.setPrimitiveLog(primLogBlock.getDefaultState(), new ItemStack(primLogBlock));
	}

	/**
	 * Gets the primitive full block (vanilla)log that represents this tree's
	 * material. Chiefly used to determine the wood hardness for harvesting
	 * behavior.
	 *
	 * @return BlockState of the primitive log.
	 */
	public BlockState getPrimitiveLog() {
		return primitiveLog;
	}

	/**
	 * Gets an itemStack of primitive logs of a requested quantity.
	 *
	 * @param qty The quantity of logs requested
	 * @return itemStack of requested logs.
	 */
	public ItemStack getPrimitiveLogItemStack(int qty) {
		ItemStack stack = primitiveLogItemStack.copy();
		stack.setCount(MathHelper.clamp(qty, 0, 64));
		return stack;
	}

	///////////////////////////////////////////
	//BRANCHES
	///////////////////////////////////////////

	public int getRadiusForCellKit(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, BlockBranch branch) {
		int radius = branch.getRadius(blockState);
		int meta = CellMetadata.NONE;
		if(hasConiferVariants && radius == 1) {
			if(blockAccess.getBlockState(pos.down()).getBlock() == branch) {
				meta = CellMetadata.CONIFERTOP;
			}
		}

		return CellMetadata.radiusAndMeta(radius, meta);
	}

	/** Thickness of a twig.. Should always be 1 unless the tree has no leaves(like a cactus) [default = 1] */
	public float getPrimaryThickness() {
		return 1.0f;
	}

	/** Thickness of the branch connected to a twig(radius == 1).. This should probably always be 2 [default = 2] */
	public float getSecondaryThickness() {
		return 2.0f;
	}

	///////////////////////////////////////////
	// SURFACE ROOTS
	///////////////////////////////////////////

	public BlockSurfaceRoot getSurfaceRoots() {
		return null;
	}

	///////////////////////////////////////////
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////

	public IAnimationHandler selectAnimationHandler(EntityFallingTree fallingEntity) {
		return fallingEntity.defaultAnimationHandler();
	}

	///////////////////////////////////////////
	// LEAVES HANDLING
	///////////////////////////////////////////

	public boolean isCompatibleDynamicLeaves(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		BlockDynamicLeaves leaves = TreeHelper.getLeaves(blockState);
		return (leaves != null) && this == leaves.getFamily(blockState, blockAccess, pos);
	}

	public interface IConnectable {
		boolean isConnectable(BlockState blockState);
	}

	LinkedList<IConnectable> vanillaConnectables = new LinkedList<>();

	public void addConnectableVanillaLeaves(IConnectable connectable) {
		vanillaConnectables.add(connectable);
	}

	public boolean isCompatibleVanillaLeaves(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {

		Block block = blockState.getBlock();

		if(!(block instanceof BlockDynamicLeaves) && block instanceof LeavesBlock) {
			for(IConnectable connectable : vanillaConnectables) {
				if(connectable.isConnectable(blockState)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean isCompatibleGenericLeaves(BlockState blockState, World blockAccess, BlockPos pos) {
		return isCompatibleDynamicLeaves(blockState, blockAccess, pos) || isCompatibleVanillaLeaves(blockState, blockAccess, pos);
	}

	/**
	 * This is used for trees that have leaves that are not cubes and require extra blockstate properties such as palm fronds.
	 * Used for tree felling animation.
	 *
	 * @return
	 */
	public HashMap<BlockPos, BlockState> getFellingLeavesClusters(BranchDestructionData destructionData) {
		return null;
	}

	public ILeavesProperties getCommonLeaves() {
		return LeavesProperties.NULLPROPERTIES;
	}

	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////

	@Override
	public String toString() {
		return getName().toString();
	}
	
}