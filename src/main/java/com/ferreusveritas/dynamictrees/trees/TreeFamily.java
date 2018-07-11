package com.ferreusveritas.dynamictrees.trees;

import java.util.LinkedList;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.items.Seed;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
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
	
	/** Get your Cheeto fingers off! Only the DynamicTrees mod should use this and only for vanilla trees */
	public TreeFamily(BlockPlanks.EnumType wood) {
		this(new ResourceLocation(ModConstants.MODID, wood.getName().replace("_","")));
		
		//Setup tree references
		boolean isOld = wood.getMetadata() < 4;
		setPrimitiveLog((isOld ? Blocks.LOG : Blocks.LOG2).getDefaultState().withProperty(isOld ? BlockOldLog.VARIANT : BlockNewLog.VARIANT, wood));
				
		//Setup common species
		getCommonSpecies().setDynamicSapling(ModBlocks.blockDynamicSapling.getDefaultState().withProperty(BlockSapling.TYPE, wood));
		getCommonSpecies().generateSeed();
	}
	
	public TreeFamily() {
		this.name = new ResourceLocation(ModConstants.MODID, "null");
	}
	
	/**
	 * Constructor suitable for derivative mods
	 * 
	 * @param modid The MODID of the mod that is registering this tree
	 * @param name The simple name of the tree e.g. "oak"
	 * @param seq The registration sequence number for this MODID. Used for registering 4 leaves types per {@link BlockDynamicLeaves}.
	 * Sequence numbers must be unique within each mod.  It's recommended to define the sequence consecutively and avoid later rearrangement. 
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
		blockList.add(dynamicBranch);
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
		itemList.add(new ItemBlock(dynamicBranch).setRegistryName(dynamicBranch.getRegistryName()));
		
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
	
	protected TreeFamily setPrimitiveLog(IBlockState primLog) {
		return setPrimitiveLog(primLog, new ItemStack(Item.getItemFromBlock(primLog.getBlock()), 1, primLog.getBlock().damageDropped(primLog)));
	}
	
	protected TreeFamily setPrimitiveLog(IBlockState primLog, ItemStack primLogStack) {
		primitiveLog = primLog;
		primitiveLogItemStack = primLogStack;
		return this;
	}
	
	public IBlockState getPrimitiveLog() {
		return primitiveLog;
	}
	
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
	
	
	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////
	
	@Override
	public String toString() {
		return getName().toString();
	}
	
}