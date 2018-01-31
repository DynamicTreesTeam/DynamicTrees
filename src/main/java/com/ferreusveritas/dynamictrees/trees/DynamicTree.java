package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
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
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

/**
* All data related to a tree family.
* A {@link DynamicTree} is more or less just a definition of {@link BlockDynamicLeaves} and {@link BlockBranch} blocks.
* It also defines the cellular automata function of the {@link BlockDynamicLeaves}.  It defines the type of wood that
* the tree is made of and consequently what kind of log you get when you cut it down.
* 
* A DynamicTree does not contain a reference to a Seed, Sapling, or how it should grow(how fast, how tall, etc).
* It does not control what drops it produces or what fruit it grows.  It does not control where it should grow.
* All of these capabilities lie in the Species class for which a DynamicTree should always contain one default 
* species(the common species).
* 
* @author ferreusveritas
*/
public class DynamicTree {
	
	public final static DynamicTree NULLTREE = new DynamicTree() {
		@Override public void setCommonSpecies(Species species) {}
		@Override public Species getCommonSpecies() { return Species.NULLSPECIES; }
		@Override public void setCellKit(ResourceLocation name) {}
		@Override public DynamicTree setDynamicLeaves(BlockDynamicLeaves leaves, int sub) { return this; }
		@Override public List<Block> getRegisterableBlocks(List<Block> blockList) { return blockList; }
		@Override public List<Item> getRegisterableItems(List<Item> itemList) { return itemList; }
		@Override public boolean onTreeActivated(World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) { return false; }
	};
	
	/** Simple name of the tree e.g. "oak" */
	private final ResourceLocation name;
	
	protected Species commonSpecies = Species.NULLSPECIES;
	
	//Branches
	/** The dynamic branch used by this tree */
	private BlockBranch dynamicBranch;
	/** The primitive(vanilla) log to base the texture, drops, and other behavior from */
	private IBlockState primitiveLog = Blocks.AIR.getDefaultState();
	/** cached ItemStack of primitive logs(what is returned when wood is harvested) */
	private ItemStack primitiveLogItemStack = CompatHelper.emptyStack();
	
	//Saplings
	/** The primitive(vanilla) sapling for this type of tree. Used for crafting recipes */
	private IBlockState primitiveSaplingBlockState = Blocks.AIR.getDefaultState();
	/** The primitive(vanilla) sapling for this type of tree. Used for crafting recipes */
	private ItemStack primitiveSaplingItemStack = CompatHelper.emptyStack();
	
	//Leaves
	/** The dynamic leaves used by this tree */
	private BlockDynamicLeaves dynamicLeaves;
	/** A dynamic leaves block needs a subblock number to specify which subblock we are working with **/
	private int leavesSubBlock;
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	private int smotherLeavesMax = 4;
	/** Minimum amount of light necessary for a leaves block to be created. **/
	protected int lightRequirement = 13;
	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	private IBlockState primitiveLeaves = Blocks.AIR.getDefaultState();
	/** cached ItemStack of primitive leaves(what is returned when leaves are sheared) */
	private ItemStack primitiveLeavesItemStack = CompatHelper.emptyStack();
	/** A CellKit for leaves automata */
	private ICellKit cellKit = TreeRegistry.findCellKit(new ResourceLocation(ModConstants.MODID, "deciduous"));
	
	
	//Misc
	/** The stick that is returned when a whole log can't be dropped */
	private ItemStack stick = new ItemStack(Items.STICK);
	/** Weather the branch can support cocoa pods on it's surface [default = false] */
	public boolean canSupportCocoa = false;
	
	/** Get your Cheeto fingers off! Only dynamictrees mod should use this and only for vanilla trees */
	public DynamicTree(BlockPlanks.EnumType treeType) {
		this(new ResourceLocation(ModConstants.MODID, treeType.getName().replace("_","")), treeType.getMetadata());
		simpleVanillaSetup(treeType);
	}
	
	public DynamicTree() {
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
	public DynamicTree(ResourceLocation name, int seq) {
		this.name = name;
		
		if(seq >= 0) {
			setDynamicLeaves(name.getResourceDomain(), seq);
		}
		setDynamicBranch(new BlockBranch(name + "branch"));
		
		createSpecies();
	}
	
	/**
	 * This is for use with Vanilla Tree types only.  Mods depending on the dynamictrees mod should 
	 * call the here contained primitive assignment functions in their constructor instead.
	 * 
	 * @param wood
	 */
	private void simpleVanillaSetup(BlockPlanks.EnumType wood) {
		
		switch(wood) {
			case OAK:
			case SPRUCE:
			case BIRCH:
			case JUNGLE: {
				IBlockState primLeaves = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, wood);
				IBlockState primLog = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, wood);
				setPrimitiveLeaves(primLeaves, new ItemStack(primLeaves.getBlock(), 1, primLeaves.getValue(BlockOldLeaf.VARIANT).getMetadata() & 3));
				setPrimitiveLog(primLog, new ItemStack(primLog.getBlock(), 1, primLog.getValue(BlockOldLog.VARIANT).getMetadata() & 3));
			}
			break;
			case ACACIA:
			case DARK_OAK: {
				IBlockState primLeaves = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, wood);
				IBlockState primLog = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, wood);
				setPrimitiveLeaves(primLeaves, new ItemStack(primLeaves.getBlock(), 1, primLeaves.getValue(BlockNewLeaf.VARIANT).getMetadata() & 3));
				setPrimitiveLog(primLog, new ItemStack(primLog.getBlock(), 1, primLog.getValue(BlockNewLog.VARIANT).getMetadata() & 3));
			}
			break;
		}
		
		setPrimitiveSapling(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, wood), new ItemStack(Blocks.SAPLING, 1, wood.getMetadata()));
		ModBlocks.blockBonsaiPot.setupVanillaTree(this);//Setup the bonsai pot to receive this type of tree

		simpleVanillaCommonSpecies(wood);
	}
	
	protected void simpleVanillaCommonSpecies(BlockPlanks.EnumType wood) {
		getCommonSpecies().setDynamicSapling(ModBlocks.blockDynamicSapling.getDefaultState().withProperty(BlockSapling.TYPE, wood));
		getCommonSpecies().generateSeed();
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

	/**
	 * This is only used by Rooty Dirt to get the appropriate species for this tree.
	 * For instance Oak may use this to select a Swamp Oak species if the coordinates 
	 * are in a swamp.
	 * 
	 * @param access
	 * @param pos
	 * @return
	 */
	public Species getSpeciesForLocation(World access, BlockPos pos) {
		return getCommonSpecies();
	}

	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {
		
		//Bonemeal fertilizes the soil and causes a single growth pulse
		if( itemStack.getItem() == Items.DYE && itemStack.getItemDamage() == 15) {
			return new SubstanceFertilize().setAmount(1).setGrow(true);
		}
		
		//Use substance provider interface if it's available
		if(itemStack.getItem() instanceof ISubstanceEffectProvider) {
			ISubstanceEffectProvider provider = (ISubstanceEffectProvider) itemStack.getItem();
			return provider.getSubstanceEffect(itemStack);
		}
		
		return null;
	}
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	/**
	* Apply an item to the treepart(e.g. bonemeal). Developer is responsible for decrementing itemStack after applying.
	* 
	* @param world The current world
	* @param hitPos Position
	* @param player The player applying the substance
	* @param itemStack The itemstack to be used.
	* @return true if item was used, false otherwise
	*/
	public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		
		ISubstanceEffect effect = getSubstanceEffect(itemStack);
		
		if(effect != null) {
			if(effect.isLingering()) {
				CompatHelper.spawnEntity(world, new EntityLingeringEffector(world, rootPos, effect));
				return true;
			} else {
				return effect.apply(world, rootPos);
			}
		}
		
		return false;
	}
	
	public boolean onTreeActivated(World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		BlockPos rootPos = findRootNode(world, hitPos);
		
		if(rootPos != null) {
			if (heldItem != null) {//Something in the hand
				if(applySubstance(world, rootPos, hitPos, player, hand, heldItem)) {
					CompatHelper.consumePlayerItem(player, hand, heldItem);
					return true;
				}
			}

			//Empty hand or inactive substance
			getExactSpecies(world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
		}

		return false;
	}

	//////////////////////////////
	// REGISTRATION
	//////////////////////////////
	
	/** Used to register the blocks this tree uses.  Mainly just the {@link BlockBranch} 
	 * We intentionally leave out leaves since they are shared between trees */
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
	
	//////////////////////////////
	// TREE PROPERTIES
	//////////////////////////////
	
	public ResourceLocation getName() {
		return name;
	}
	
	/**
	 * Sets the Dynamic Leaves for this tree.
	 * 
	 * @param leaves The Dynamic Leaves Block
	 * @param sub The subtype number (0-3) for using 4 leaves type per {@link BlockDynamicLeaves} (e.g. oak=0, spruce=1, etc)
	 * @return this tree for chaining
	 */
	public DynamicTree setDynamicLeaves(BlockDynamicLeaves leaves, int sub) {
		dynamicLeaves = leaves;
		leavesSubBlock = sub;
		dynamicLeaves.setTree(leavesSubBlock, this);
		return this;
	}
	
	/**
	 * Set dynamic leaves from an automatically created source.
	 * 
	 * @param modid The MODID of the mod that is defining this tree
	 * @param seq The sequencing number(see constructor for details)
	 * @return this tree for chaining
	 */
	protected DynamicTree setDynamicLeaves(String modid, int seq) {
		return setDynamicLeaves(TreeHelper.getLeavesBlockForSequence(modid, seq), seq & 3);
	}
	
	public BlockDynamicLeaves getDynamicLeaves() {
		return dynamicLeaves;
	}
	
	public int getDynamicLeavesSub() {
		return leavesSubBlock;
	}
	
	public IBlockState getDynamicLeavesState() {
		return getDynamicLeaves().getDefaultState().withProperty(BlockDynamicLeaves.TREE, this.getDynamicLeavesSub());
	}
	
	public IBlockState getDynamicLeavesState(int hydro) {
		return getDynamicLeavesState().withProperty(BlockDynamicLeaves.HYDRO, MathHelper.clamp(hydro, 1, 4));
	}
	
	protected DynamicTree setDynamicBranch(BlockBranch gBranch) {
		dynamicBranch = gBranch;//Link the tree to the branch
		dynamicBranch.setTree(this);//Link the branch back to the tree
		return this;
	}
	
	public BlockBranch getDynamicBranch() {
		return dynamicBranch;
	}
	
	protected DynamicTree setStick(ItemStack itemStack) {
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
		return CompatHelper.setStackCount(stick.copy(), MathHelper.clamp(qty, 0, 64));
	}
	
	protected DynamicTree setPrimitiveLeaves(IBlockState primLeaves, ItemStack primLeavesStack) {
		primitiveLeaves = primLeaves;
		primitiveLeavesItemStack = primLeavesStack;
		return this;
	}
	
	public IBlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}
	
	public ItemStack getPrimitiveLeavesItemStack(int qty) {
		return CompatHelper.setStackCount(primitiveLeavesItemStack.copy(), MathHelper.clamp(qty, 0, 64));
	}
	
	protected DynamicTree setPrimitiveLog(IBlockState primLog, ItemStack primLogStack) {
		primitiveLog = primLog;
		primitiveLogItemStack = primLogStack;
		return this;
	}
	
	public IBlockState getPrimitiveLog() {
		return primitiveLog;
	}

	public ItemStack getPrimitiveLogItemStack(int qty) {
		return CompatHelper.setStackCount(primitiveLogItemStack.copy(), MathHelper.clamp(qty, 0, 64));
	}
	
	protected DynamicTree setPrimitiveSapling(IBlockState primSapling, ItemStack primSaplingStack) {
		primitiveSaplingBlockState = primSapling;
		primitiveSaplingItemStack = primSaplingStack;
		return this;
	}

	public IBlockState getPrimitiveSaplingBlockState() {
		return primitiveSaplingBlockState;
	}
	
	public ItemStack getPrimitiveSaplingItemStack() {
		return primitiveSaplingItemStack;
	}
	
	///////////////////////////////////////////
	//BRANCHES
	///////////////////////////////////////////
	
	
	/**
	 * This is resource intensive.  Use only for interaction code.
	 * Only the root node can determine the exact species and it has
	 * to be found by mapping the branch network.
	 * 
	 * @param world
	 * @param pos
	 * @return
	 */
	public static Species getExactSpecies(World world, BlockPos pos) {
		
		BlockPos rootPos = findRootNode(world, pos);
		if(rootPos != null) {
			BlockRootyDirt rootBlock = (BlockRootyDirt) world.getBlockState(rootPos).getBlock();
			return rootBlock.getSpecies(world, rootPos);
		}
		
		return Species.NULLSPECIES;
	}
	

	public static BlockPos findRootNode(World world, BlockPos pos) {
		
		ITreePart treePart = TreeHelper.getSafeTreePart(world, pos);

		if(treePart.isRootNode()) {
			return pos;
		}
		
		if(treePart.isBranch()) {
			MapSignal signal = treePart.analyse(world, pos, null, new MapSignal());// Analyze entire tree network to find root node
			if(signal.found) {
				return signal.root;
			}
		}
		
		return null;
	}
	
	public ICell getCellForBranch(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		return getCellKit().getCellForBranch(branch.getRadius(blockState));
	}
	
	
	///////////////////////////////////////////
	//RENDERING
	///////////////////////////////////////////
	
	@SideOnly(Side.CLIENT)
	public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(world != null && pos != null) {
			return BiomeColorHelper.getFoliageColorAtPos(world, pos);
		}
		return ColorizerFoliage.getFoliageColorBasic();
	}
	
	///////////////////////////////////////////
	// LEAVES AUTOMATA
	///////////////////////////////////////////
	
	public void setSmotherLeavesMax(int smotherLeavesMax) {
		this.smotherLeavesMax = smotherLeavesMax;
	}
	
	public int getSmotherLeavesMax() {
		return smotherLeavesMax;
	}
	
	/** Minimum amount of light necessary for a leaves block to be created. **/
	public int getLightRequirement() {
		return lightRequirement;
	}
	
	public void setCellKit(String name) {
		cellKit = TreeRegistry.findCellKit(name);
	}
	
	public void setCellKit(ResourceLocation name) {
		cellKit = TreeRegistry.findCellKit(name);
	}
	
	public ICellKit getCellKit() {
		return cellKit;
	}
	
	
	//////////////////////////////
	// LEAVES HANDLING
	//////////////////////////////
	
	public boolean isCompatibleDynamicLeaves(IBlockAccess blockAccess, BlockPos pos) {
		
		IBlockState state = blockAccess.getBlockState(pos);
		ITreePart treePart = TreeHelper.getTreePart(state);
		
		if (treePart != null && treePart instanceof BlockDynamicLeaves) {
			return this == ((BlockDynamicLeaves)treePart).getTree(state);			
		}
		
		return false;
	}
	
	public boolean isCompatibleDynamicLeaves(Block leaves, int sub) {
		return leaves == getDynamicLeaves() && sub == getDynamicLeavesSub();
	}
	
	public boolean isCompatibleVanillaLeaves(IBlockAccess blockAccess, BlockPos pos) {
		IBlockState primState = getPrimitiveLeaves();
		IBlockState otherState = blockAccess.getBlockState(pos);

		Block primBlock = primState.getBlock();
		Block otherBlock = otherState.getBlock();
		
		if(primBlock == otherBlock) {//Blocks Match
			//Does it break the BlockState convention? You bet.  Do I not care? You bet!
			return ((primBlock.getMetaFromState(primState) & 3) == (otherBlock.getMetaFromState(otherState) & 3));
		}
		
		return false;
	}
	
	public boolean isCompatibleGenericLeaves(IBlockAccess blockAccess, BlockPos pos) {
		return isCompatibleDynamicLeaves(blockAccess, pos) || isCompatibleVanillaLeaves(blockAccess, pos);
	}
	
	public ICell getCellForLeaves(int hydro) {
		return getCellKit().getCellForLeaves(hydro);
	}
	
	
	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////

	static private final EnumFacing upFirst[] = {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
	
	/**
	* Handle rotting branches
	* @param world The world
	* @param pos
	* @param neighborCount Count of neighbors reinforcing this block
	* @param radius The radius of the branch
	* @param random Access to a random number generator
	* @return true if the branch should rot
	*/
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
		
		if(radius <= 1) {
			for(EnumFacing dir: upFirst) {
				if(getDynamicLeaves().growLeaves(world, this, pos.offset(dir), 0)) {
					return false;
				}
			}
		}
		world.setBlockToAir(pos);
		return true;
	}
	

	//////////////////////////////
	// BONSAI POT
	//////////////////////////////
	
	/**
	 * Provides the {@link BlockBonsaiPot} for this tree.  Each mod will
	 * have to derive it's own BonzaiPot subclass if it wants this feature.
	 * 
	 * @return
	 */
	public BlockBonsaiPot getBonzaiPot() {
		return ModBlocks.blockBonsaiPot;
	}

	
	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////
	
	@Override
	public String toString() {
		return getName().toString();
	}
	
}