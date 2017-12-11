package com.ferreusveritas.dynamictrees.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.cells.ICellSolver;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
import com.ferreusveritas.dynamictrees.potion.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

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
import net.minecraftforge.registries.IForgeRegistry;

/**
* All data related to a tree family.
* 
* @author ferreusveritas
*/
public abstract class DynamicTree {
	
	/** Simple name of the tree e.g. "oak" */
	private final ResourceLocation name;

	//Branches
	/** The dynamic branch used by this tree */
	private BlockBranch dynamicBranch;
	/** The primitive(vanilla) log to base the texture, drops, and other behavior from */
	private IBlockState primitiveLog;
	/** cached ItemStack of primitive logs(what is returned when wood is harvested) */
	private ItemStack primitiveLogItemStack;
	/** The primitive(vanilla) sapling for this type of tree. Used for crafting recipes */
	private IBlockState primitiveSapling;
	
	//Leaves
	/** The dynamic leaves used by this tree */
	private BlockDynamicLeaves dynamicLeaves;
	/** A dynamic leaves block needs a subblock number to specify which subblock we are working with **/
	private int leavesSubBlock;
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	private int smotherLeavesMax = 4;
	/** Minimum amount of light necessary for a leaves block to be created. **/
	private int lightRequirement = 13;
	/** The default hydration level of a newly created leaf block [default = 4]**/
	protected byte defaultHydration = 4;
	/** The primitive(vanilla) leaves are used for many purposes including rendering, drops, and some other basic behavior. */
	private IBlockState primitiveLeaves;
	/** cached ItemStack of primitive leaves(what is returned when leaves are sheared) */
	private ItemStack primitiveLeavesItemStack;
	/** A voxel map of leaves blocks that are "stamped" on to the tree during generation */
	private SimpleVoxmap leafCluster;
	/** The solver used to calculate the leaves hydration value from the values pulled from adjacent cells [default = deciduous] */
	private ICellSolver cellSolver = Cells.deciduousSolver;
	

	//Misc
	/** The stick that is returned when a whole log can't be dropped */
	private ItemStack stick;
	/** Weather the branch can support cocoa pods on it's surface [default = false] */
	public boolean canSupportCocoa = false;
	
	/** Get your Cheeto fingers off! Only dynamictrees mod should use this and only for vanilla trees */
	public DynamicTree(BlockPlanks.EnumType treeType) {
		this(new ResourceLocation(ModConstants.MODID, treeType.getName().replace("_","")), treeType.getMetadata());
		simpleVanillaSetup(treeType);
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
		setStick(new ItemStack(Items.STICK));
		
		createLeafCluster();
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
		
		setPrimitiveSapling(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, wood));

		simpleVanillaCommonSpecies(wood);
	}
	
	protected void simpleVanillaCommonSpecies(BlockPlanks.EnumType wood) {

		Species commonSpecies = (Species) getCommonSpecies();
					
		commonSpecies.setDynamicSapling(ModBlocks.blockDynamicSapling.getDefaultState().withProperty(BlockSapling.TYPE, wood));
	
		//Generate a seed
		commonSpecies.generateSeed();
		
		//A JoCode models for worldgen
		commonSpecies.addJoCodes();
	}
	
	public abstract void createSpecies();
	
	public abstract void registerSpecies(IForgeRegistry<Species> speciesRegistry);
	
	public abstract Species getCommonSpecies();

	/**
	 * This is only used by Rooty Dirt to get the appropriate species for this tree.
	 * For instance Oak may use this to select a Swamp Oak species if the coordinates 
	 * are in a swamp.
	 * 
	 * @param access
	 * @param pos
	 * @return
	 */
	public Species getSpeciesForLocation(IBlockAccess access, BlockPos pos) {
		return getCommonSpecies();
	}

	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {
		
		//Bonemeal fertilizes the soil
		if( itemStack.getItem() == Items.DYE && itemStack.getItemDamage() == 15) {
			return new SubstanceFertilize().setAmount(1);
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
			Species species = getExactSpecies(world, hitPos);
			if(species != null) {
				species.onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
			}
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
	
	protected DynamicTree setPrimitiveSapling(IBlockState primSapling) {
		primitiveSapling = primSapling;
		return this;
	}

	public IBlockState getPrimitiveSapling() {
		return primitiveSapling;
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
		
		return null;
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
		return branch.getRadius(blockState) == 1 ? Cells.branchCell : Cells.nullCell;
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
	
	public byte getDefaultHydration() {
		return defaultHydration;
	}
	
	public void setCellSolver(ICellSolver solver) {
		cellSolver = solver;
	}
	
	public ICellSolver getCellSolver() {
		return cellSolver;
	}
		
	public void setLeafCluster(SimpleVoxmap leafCluster) {
		this.leafCluster = leafCluster;
	}
	
	public SimpleVoxmap getLeafCluster() {
		return leafCluster;
	}
	
	/**
	 * A voxelmap of a leaf cluser for this species.  Values represent hydration value.
	 * This leaf cluster map is "stamped" on to each branch end during worldgen.  Should be
	 * representative of what the species actually produces.
	 */
	public void createLeafCluster(){

		leafCluster = new SimpleVoxmap(5, 4, 5, new byte[] {
				//Layer 0 (Bottom)
				0, 0, 0, 0, 0,
				0, 1, 1, 1, 0,
				0, 1, 1, 1, 0,
				0, 1, 1, 1, 0,
				0, 0, 0, 0, 0,

				//Layer 1
				0, 1, 1, 1, 0,
				1, 3, 4, 3, 1,
				1, 4, 0, 4, 1,
				1, 3, 4, 3, 1,
				0, 1, 1, 1, 0,
				
				//Layer 2
				0, 1, 1, 1, 0,
				1, 2, 3, 2, 1,
				1, 3, 4, 3, 1,
				1, 2, 3, 2, 1,
				0, 1, 1, 1, 0,
				
				//Layer 3(Top)
				0, 0, 0, 0, 0,
				0, 1, 1, 1, 0,
				0, 1, 1, 1, 0,
				0, 1, 1, 1, 0,
				0, 0, 0, 0, 0,
				
		}).setCenter(new BlockPos(2, 1, 2));

	}
	
	public byte getLeafClusterPoint(BlockPos twigPos, BlockPos leafPos) {
		return leafCluster.getVoxel(twigPos, leafPos);
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
		return Cells.normalCells[hydro];
	}
	
	//////////////////////////////
	// DROPS HANDLING
	//////////////////////////////
	
	/** 
	* Override to add items to the included list argument. For apples and whatnot.
	* Pay Attention!  Add items to drops parameter.
	* 
	* @param world
	* @param pos
	* @param chance
	* @param drops
	* @return
	*/
	public ArrayList<ItemStack> getDrops(IBlockAccess blockAccess, BlockPos pos, int chance, ArrayList<ItemStack> drops) {
		return drops;
	}
	
	
	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////
	
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
		
		final EnumFacing upFirst[] = {EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
		
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