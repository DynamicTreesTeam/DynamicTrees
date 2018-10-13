package com.ferreusveritas.dynamictrees.trees;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

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
		@Override public boolean onTreeActivated(World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) { return false; }
		@Override public ItemStack getStick(int qty) { return ItemStack.EMPTY; }
	};
	
	/** Simple name of the tree e.g. "oak" */
	private final ResourceLocation name;
	
	protected Species commonSpecies = Species.NULLSPECIES;
	
	//Branches
	/** The dynamic branch used by this tree family */
	private BlockBranch dynamicBranch;
	/** The primitive(vanilla) log to base the texture, drops, and other behavior from */
	private IBlockState primitiveLog = Blocks.AIR.getDefaultState();
	/** cached ItemStack of primitive logs(what is returned when wood is harvested) */
	private ItemStack primitiveLogItemStack = ItemStack.EMPTY;
	
	//Misc
	/** The stick that is returned when a whole log can't be dropped */
	private ItemStack stick;
	/** Weather the branch can support cocoa pods on it's surface [default = false] */
	public boolean canSupportCocoa = false;
	
	public TreeFamily() {
		this.name = new ResourceLocation(ModConstants.MODID, "null");
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
	
	public void setCommonSpecies(Species species) {
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
			if(species != Species.NULLSPECIES) {
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
	
	public boolean onTreeActivated(World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		BlockPos rootPos = TreeHelper.findRootNode(state, world, hitPos);
		
		if(rootPos != BlockPos.ORIGIN) {
			TreeHelper.getExactSpecies(state, world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
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
		blockList.add(getDynamicBranch());
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
		itemList.add(new ItemBlock(branch).setRegistryName(branch.getRegistryName()));
		
		Seed seed = getCommonSpecies().getSeed();
		if(seed != Seed.NULLSEED) {
			itemList.add(seed);
		}
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
		return new BlockBranchBasic(name + "branch");
	}
	
	protected TreeFamily setDynamicBranch(BlockBranch gBranch) {
		dynamicBranch = gBranch;//Link the tree to the branch
		dynamicBranch.setFamily(this);//Link the branch back to the tree
		
		return this;
	}
	
	public BlockBranch getDynamicBranch() {
		return dynamicBranch;
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
	protected TreeFamily setPrimitiveLog(IBlockState primLog) {
		return setPrimitiveLog(primLog, new ItemStack(Item.getItemFromBlock(primLog.getBlock()), 1, primLog.getBlock().damageDropped(primLog)));
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
	protected TreeFamily setPrimitiveLog(IBlockState primLog, ItemStack primLogStack) {
		primitiveLog = primLog;
		primitiveLogItemStack = primLogStack;
		return this;
	}
	
	/**
	 * Gets the primitive full block (vanilla)log that represents this tree's 
	 * material. Chiefly used to determine the wood hardness for harvesting
	 * behavior.
	 * 
	 * @return BlockState of the primitive log.
	 */
	public IBlockState getPrimitiveLog() {
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
	
	public int getRadiusForCellKit(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		return branch.getRadius(blockState);
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
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////
	
	public IAnimationHandler selectAnimationHandler(EntityFallingTree fallingEntity) {
		return fallingEntity.defaultAnimationHandler();
	}
	
	///////////////////////////////////////////
	// LEAVES HANDLING
	///////////////////////////////////////////
	
	public boolean isCompatibleDynamicLeaves(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		BlockDynamicLeaves leaves = TreeHelper.getLeaves(blockState);
		return (leaves != null) && this == leaves.getFamily(blockState, blockAccess, pos);
	}
	
	public interface IConnectable {
		boolean isConnectable(IBlockState blockState);
	}
	
	LinkedList<IConnectable> vanillaConnectables = new LinkedList<>(); 
	
	public void addConnectableVanillaLeaves(IConnectable connectable) {
		vanillaConnectables.add(connectable);
	}
	
	public boolean isCompatibleVanillaLeaves(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		
		Block block = blockState.getBlock();
		
		if(!(block instanceof BlockDynamicLeaves) && block instanceof BlockLeaves) {
			for(IConnectable connectable : vanillaConnectables) {
				if(connectable.isConnectable(blockState)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isCompatibleGenericLeaves(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return isCompatibleDynamicLeaves(blockState, blockAccess, pos) || isCompatibleVanillaLeaves(blockState, blockAccess, pos);
	}
	
	/**
	 * This is used for trees that have leaves that are not cubes and require extra blockstate properties such as palm fronds.
	 * Used for tree felling animation.
	 * 
	 * @return
	 */
	public HashMap<BlockPos, IBlockState> getFellingLeavesClusters(BranchDestructionData destructionData) {
		return null;
	}
	
	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////
	
	@Override
	public String toString() {
		return getName().toString();
	}
	
}