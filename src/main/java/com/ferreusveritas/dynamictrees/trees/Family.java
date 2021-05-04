package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BasicBranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.cells.MetadataCell;
import com.ferreusveritas.dynamictrees.compat.WailaOther;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This structure describes a Family whose member Species all have a common branch.
 *
 * A {@link Family} is more or less just a definition of {@link BranchBlock} blocks.
 * It also defines the cellular automata function of the {@link BranchBlock}.  It defines the type of wood that
 * the tree is made of and consequently what kind of log you get when you cut it down.
 *
 * A DynamicTree does not contain a reference to a Seed, Leaves, Sapling, or how it should grow(how fast, how tall, etc).
 * It does not control what drops it produces or what fruit it grows.  It does not control where it should grow.
 * All of these capabilities lie in the Species class for which a DynamicTree should always contain one default
 * species(the common species).
 *
 * @author ferreusveritas
 */
public class Family extends RegistryEntry<Family> implements IResettable<Family> {

	public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(Family::new);

	public final static Family NULL_FAMILY = new Family() {
		@Override public void setupCommonSpecies(Species species) {}
		@Override public Species getCommonSpecies() { return Species.NULL_SPECIES; }
		@Override public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, BlockRayTraceResult hit) { return false; }
		@Override public ItemStack getStick(int qty) { return ItemStack.EMPTY; }
		@Override public BranchBlock getValidBranchBlock(int index) { return null; }
		@Override public Species getSpeciesForLocation(IWorld world, BlockPos trunkPos) { return Species.NULL_SPECIES; }
		@Override public void addConnectableVanillaLeaves(IConnectable connectable) {}
	};

	/**
	 * Central registry for all {@link Family} objects.
	 */
	public static final TypedRegistry<Family> REGISTRY = new TypedRegistry<>(Family.class, NULL_FAMILY, TYPE);

	protected Species commonSpecies;

	protected LeavesProperties commonLeaves = LeavesProperties.NULL_PROPERTIES;

	//Branches
	/** The dynamic branch used by this tree family */
	private BranchBlock branch;
	/** The stripped variant of the branch used by this tree family */
	private BranchBlock strippedBranch;
	/** The dynamic branch's block item */
	private Item branchItem;
	/** The surface root used by this tree family */
	private SurfaceRootBlock surfaceRoot;
	/** The primitive (vanilla) log to base the texture, drops, and other behavior from */
	private Block primitiveLog = Blocks.AIR;
	/** The primitive stripped log to base the texture, drops, and other behavior from */
	private Block primitiveStrippedLog = Blocks.AIR;

	/** A list of branches the tree accepts as its own. Used for the falling tree renderer */
	private final List<BranchBlock> validBranches = new LinkedList<>();

	/** The maximum radius of a {@link BranchBlock} belonging to this family. {@link Species#maxBranchRadius} will be clamped to this value. */
	private int maxBranchRadius = BranchBlock.MAX_RADIUS;

	//Leaves
	/** Used to modify the getRadiusForCellKit call to create a special case */
	protected boolean hasConiferVariants = false;

	protected boolean hasSurfaceRoot = false;
	protected boolean hasStrippedBranch = true;

	//Misc
	/** The stick that is returned when a whole log can't be dropped */
	private Item stick = Items.STICK;
	/** Weather the branch can support cocoa pods on it's surface [default = false] */
	public boolean canSupportCocoa = false;

	@OnlyIn(Dist.CLIENT)
	public int woodRingColor; // For rooty blocks
	@OnlyIn(Dist.CLIENT)
	public int woodBarkColor; // For rooty water

	/** A list of child species, added to when tree family is set for species. */
	private final Set<Species> species = new HashSet<>();

	private Family() {
		this.setRegistryName(DTTrees.NULL);
	}

	/**
	 * Constructor suitable for derivative mods
	 *
	 * @param name The ResourceLocation of the tree e.g. "mymod:poplar"
	 */
	public Family(ResourceLocation name) {
		this.setRegistryName(name);
		this.commonSpecies = Species.NULL_SPECIES;
	}

	public void setupBlocks() {
		this.setBranch(this.createBranch());
		this.setBranchItem(this.createBranchItem(this.getBranchRegName(""), this.branch));

		if (this.hasStrippedBranch()) {
			this.setStrippedBranch(this.createBranch(this.getBranchRegName("stripped_")));
		}

		if (this.hasSurfaceRoot()) {
			this.setSurfaceRoot(this.createSurfaceRoot());
		}
	}

	public void setCommonSpecies(final Species species) {
		this.commonSpecies = species;
	}

	public void setupCommonSpecies(final Species species) {
		// Set the common species and auto-generate seeds and saplings unless opted out.
		this.commonSpecies = species.setShouldGenerateSeedIfNull(true).setShouldGenerateSaplingIfNull(true)
				.generateSeed().generateSapling();
	}

	public Species getCommonSpecies() {
		return commonSpecies;
	}

	public Family addSpecies (final Species species) {
		this.species.add(species);
		return this;
	}

	public Set<Species> getSpecies () {
		return this.species;
	}

	///////////////////////////////////////////
	// SPECIES LOCATION OVERRIDES
	///////////////////////////////////////////

	/**
	 * This is only used by Rooty Dirt to get the appropriate species for this tree.
	 * For instance Oak may use this to select a Swamp Oak species if the coordinates
	 * are in a swamp.
	 *
	 * @param world The {@link IWorld} object.
	 * @param trunkPos The {@link BlockPos} of the trunk.
	 * @return The {@link Species} to place.
	 */
	public Species getSpeciesForLocation(IWorld world, BlockPos trunkPos) {
		for (final Species species : this.species) {
			if (species.shouldOverrideCommon(world, trunkPos))
				return species;
		}

		return this.commonSpecies;
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, @Nullable ItemStack heldItem, BlockRayTraceResult hit) {

		if (this.canSupportCocoa) {
			BlockPos pos = hit.getBlockPos();
			if(heldItem != null) {
				if(heldItem.getItem() == Items.COCOA_BEANS) {
					BranchBlock branch = TreeHelper.getBranch(state);
					if(branch != null && branch.getRadius(state) == 8) {
						if(hit.getDirection() != Direction.UP && hit.getDirection() != Direction.DOWN) {
							pos = pos.relative(hit.getDirection());
						}
						if (world.isEmptyBlock(pos)) {
							BlockState cocoaState = DTRegistries.COCOA_FRUIT.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(player, hand, hit)));
							assert cocoaState != null;
							Direction facing = cocoaState.getValue(HorizontalBlock.FACING);
							world.setBlock(pos, DTRegistries.COCOA_FRUIT.defaultBlockState().setValue(HorizontalBlock.FACING, facing), 2);
							if (!player.isCreative()) {
								heldItem.shrink(1);
							}
							return true;
						}
					}
				}
			}
		}

		BlockPos rootPos = TreeHelper.findRootNode(world, hitPos);

		if (canStripBranch(state, world, hitPos, player, heldItem)){
			return stripBranch(state, world, hitPos, player, heldItem);
		}

		if(rootPos != BlockPos.ZERO) {
			return TreeHelper.getExactSpecies(world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, hit);
		}

		return false;
	}

	public boolean canStripBranch(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem){
		BranchBlock branchBlock = TreeHelper.getBranch(state);
		if (branchBlock == null) return false;
		return branchBlock.canBeStripped(state, world, pos, player, heldItem);
	}

	public boolean stripBranch(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem){
		if (getStrippedBranch() != null) {
			this.getBranch().stripBranch(state, world, pos, player, heldItem);

			if (world.isClientSide) {
				world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
				WailaOther.invalidateWailaPosition();
			}
			return true;
		}
		else return false;
	}


	///////////////////////////////////////////
	// TREE PROPERTIES
	///////////////////////////////////////////

	public boolean isWood() {
		return true;
	}

	/**
	 * Creates the branch block. Can be overridden by sub-classes who want full control over
	 * registry and instantiation of the branch.
	 *
	 * @return The instantiated and registered {@link BranchBlock}.
	 */
	public BranchBlock createBranch() {
		return this.createBranch(this.getBranchRegName(""));
	}

	/**
	 * Gets a branch name with the given prefix and <tt>_branch</tt> as the suffix.
	 *
	 * @param prefix The prefix.
	 * @return The {@link ResourceLocation} registry name for the branch.
	 */
	private ResourceLocation getBranchRegName(final String prefix) {
		return ResourceLocationUtils.suffix(ResourceLocationUtils.prefix(this.getRegistryName(), prefix), "_branch");
	}

	/**
	 * Instantiates and sets up the actual {@link BranchBlock} object. Can be overridden by
	 * sub-classes for custom branch blocks.
	 *
	 * @return The instantiated {@link BranchBlock}.
	 */
	protected BranchBlock createBranchBlock() {
		final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(this.getProperties()) : new BasicBranchBlock(this.getProperties());

		if (this.isFireProof())
			branch.setFireSpreadSpeed(0).setFlammability(0);

		return branch;
	}

	/**
	 * Creates branch block and adds it to the relevant {@link RegistryHandler}.
	 *
	 * @param registryName The {@link ResourceLocation} registry name.
	 * @return The created {@link BranchBlock}.
	 */
	protected BranchBlock createBranch(final ResourceLocation registryName) {
		return RegistryHandler.addBlock(registryName, this.createBranchBlock());
	}

	/**
	 * Creates and registers a {@link BlockItem} for the given branch with the given
	 * registry name.
	 *
	 * @param registryName The {@link ResourceLocation} registry name for the item.
	 * @param branch The {@link BranchBlock} to create the {@link BlockItem} for.
	 * @return The created and registered {@link BlockItem} object.
	 */
	public BlockItem createBranchItem (final ResourceLocation registryName, final BranchBlock branch) {
		return RegistryHandler.addItem(registryName, new BlockItem(branch, new Item.Properties()));
	}

	protected Family setBranch(final BranchBlock branch) {
		this.branch = this.setupBranch(branch, this.hasStrippedBranch);
		return this;
	}

	protected Family setStrippedBranch(final BranchBlock branch) {
		this.strippedBranch = this.setupBranch(branch, false);
		return this;
	}

	protected BranchBlock setupBranch (final BranchBlock branchBlock, final boolean canBeStripped) {
		branchBlock.setFamily(this); // Link the branch to the tree.
	branchBlock.setCanBeStripped(canBeStripped);
		this.addValidBranches(branchBlock); // Add the branch as a valid branch.
		return branchBlock;
	}

	protected Family setBranchItem(Item branchItem) {
		this.branchItem = branchItem;
		return this;
	}

	@Nullable
	public BranchBlock getBranch() {
		return branch;
	}

	@Nullable
	public BranchBlock getStrippedBranch() {
		return strippedBranch;
	}

	public Item getBranchItem() {
		return branchItem;
	}

	public boolean isThick() {
		return this.maxBranchRadius > BranchBlock.MAX_RADIUS;
	}

	public int getMaxBranchRadius() {
		return this.maxBranchRadius;
	}

	public void setMaxBranchRadius(int maxBranchRadius) {
		this.maxBranchRadius = maxBranchRadius;
	}

	@OnlyIn(Dist.CLIENT)
	public int getRootColor(BlockState state, boolean getBark) {
		return getBark? woodBarkColor : woodRingColor;
	}

	public void setHasConiferVariants(boolean hasConiferVariants) {
		this.hasConiferVariants = hasConiferVariants;
	}

	/**
	 * Used to set the type of stick that a tree drops when there's not enough wood volume for a log.
	 *
	 * @param item An itemstack of the stick
	 * @return {@link Family} for chaining calls
	 */
	protected Family setStick(Item item) {
		stick = item;
		return this;
	}

	/**
	 * Get a quantity of whatever is considered a stick for this tree's type of wood.
	 *
	 * @param qty Number of sticks
	 * @return an {@link ItemStack} of sticky things
	 */
	public ItemStack getStick(int qty) {
		return this.stick == Items.AIR ? ItemStack.EMPTY : new ItemStack(this.stick, MathHelper.clamp(qty, 0, 64));
	}

	public void setCanSupportCocoa(boolean canSupportCocoa) {
		this.canSupportCocoa = canSupportCocoa;
	}

	/**
	 * Used to set the type of log item that a tree drops when it's harvested.
	 * Use this function to explicitly set the itemstack instead of having it
	 * done automatically.
	 *
	 * @param primitiveLog A block object that is the log
	 * @param primitiveLog An itemStack of the log item
	 * @return {@link Family} for chaining calls
	 */
	protected Family setPrimitiveLog(Block primitiveLog) {
		this.primitiveLog = primitiveLog;

		if (this.branch != null)
			this.branch.setPrimitiveLogDrops(new ItemStack(primitiveLog));

		return this;
	}

	protected Family setPrimitiveStrippedLog(Block primitiveStrippedLog) {
		this.primitiveStrippedLog = primitiveStrippedLog;

		if (this.strippedBranch != null)
			this.strippedBranch.setPrimitiveLogDrops(new ItemStack(primitiveStrippedLog));

		return this;
	}

	/**
	 * Gets the primitive full block (vanilla)log that represents this tree's
	 * material. Chiefly used to determine the wood hardness for harvesting
	 * behavior.
	 *
	 * @return Block of the primitive log.
	 */
	public Block getPrimitiveLog() {
		return primitiveLog;
	}

	public Block getPrimitiveStrippedLog() {
		return primitiveStrippedLog;
	}

	private List<ItemStack> getLogDropsForBranch(float volume, int branch) {
		BranchBlock branchBlock = getValidBranchBlock(branch);
		List<ItemStack> logs = new LinkedList<>();
		if (branchBlock != null){
			branchBlock.getPrimitiveLogs(volume, logs);
		}
		return logs;
	}

	public boolean isFireProof () { return false; }

	public SoundType getBranchSoundType (BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		return this.getDefaultBranchSoundType();
	}

	public ToolType getBranchHarvestTool(BlockState state){
		return ToolType.AXE;
	}

	public int getBranchHarvestLevel(BlockState state){
		return 0;
	}

	public Material getDefaultBranchMaterial() {
		return Material.WOOD;
	}

	public SoundType getDefaultBranchSoundType() {
		return SoundType.WOOD;
	}

	public ToolType getDefaultBranchTool() {
		return ToolType.AXE;
	}

	public AbstractBlock.Properties getDefaultBranchProperties(final Material material, final MaterialColor materialColor) {
		return AbstractBlock.Properties.of(material, materialColor).sound(this.getDefaultBranchSoundType())
				.noDrops().harvestLevel(0).harvestTool(this.getDefaultBranchTool());
	}

	private AbstractBlock.Properties properties;

	/**
	 * Gets the {@link #properties} for this {@link Family} object.
	 *
	 * @return The {@link #properties} for this {@link Family} object.
	 */
	public AbstractBlock.Properties getProperties() {
		return this.properties == null ? this.getDefaultBranchProperties(this.getDefaultBranchMaterial(),
				this.getDefaultBranchMaterial().getColor()) : this.properties;
	}

	/**
	 * Sets the {@link #properties} for this {@link Family} object
	 * to the given {@code properties}.
	 *
	 * @param properties The new {@link AbstractBlock.Properties} object to set.
	 * @return This {@link Family} object for chaining.
	 */
	public Family setProperties(AbstractBlock.Properties properties) {
		this.properties = properties;
		return this;
	}

	///////////////////////////////////////////
	//BRANCHES
	///////////////////////////////////////////

	public int getRadiusForCellKit(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, BranchBlock branch) {
		int radius = branch.getRadius(blockState);
		int meta = MetadataCell.NONE;
		if(hasConiferVariants && radius == 1) {
			if(blockAccess.getBlockState(pos.below()).getBlock() == branch) {
				meta = MetadataCell.CONIFERTOP;
			}
		}

		return MetadataCell.radiusAndMeta(radius, meta);
	}

	/** Thickness of a twig [default = 1] */
	public int getPrimaryThickness() {
		return 1;
	}

	/** Thickness of the branch connected to a twig (radius == getPrimaryThickness) [default = 2] */
	public int getSecondaryThickness() {
		return 2;
	}

	public boolean hasStrippedBranch() {
		return this.hasStrippedBranch;
	}

	public void setHasStrippedBranch(boolean hasStrippedBranch) {
		this.hasStrippedBranch = hasStrippedBranch;
	}

	public void addValidBranches (BranchBlock... branches){
		this.validBranches.addAll(Arrays.asList(branches));
	}

	public int getBranchBlockIndex (BranchBlock block){
		int index = this.validBranches.indexOf(block);
		if (index < 0){
			LogManager.getLogger().warn("Block {} not valid branch for {}.", block, this);
			return 0;
		}
		return index;
	}

	@Nullable
	public BranchBlock getValidBranchBlock (int index) {
		return this.validBranches.get(index);
	}

	///////////////////////////////////////////
	// SURFACE ROOTS
	///////////////////////////////////////////

	public boolean hasSurfaceRoot () {
		return this.hasSurfaceRoot;
	}

	public void setHasSurfaceRoot(boolean hasSurfaceRoot) {
		this.hasSurfaceRoot = hasSurfaceRoot;
	}

	public SurfaceRootBlock createSurfaceRoot () {
		return RegistryHandler.addBlock(ResourceLocationUtils.suffix(this.getRegistryName(), "_root"), new SurfaceRootBlock(this));
	}

	public SurfaceRootBlock getSurfaceRoot() {
		return this.surfaceRoot;
	}

	protected Family setSurfaceRoot (SurfaceRootBlock surfaceRoot) {
		this.surfaceRoot = surfaceRoot;
		return this;
	}

	///////////////////////////////////////////
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////

	public IAnimationHandler selectAnimationHandler(FallingTreeEntity fallingEntity) {
		return fallingEntity.defaultAnimationHandler();
	}

	///////////////////////////////////////////
	// LEAVES HANDLING
	///////////////////////////////////////////

	/**
	 * When destroying leaves, an area is created from the branch endpoints to look for leaves blocks and destroy them.
	 * This area is then expanded by a certain size to make sure it covers all the leaves in the canopy.
	 * @return the expanded block bounds.
	 */
	public BlockBounds expandLeavesBlockBounds(BlockBounds bounds){
		return bounds.expand(3);
	}

	public boolean isCompatibleDynamicLeaves(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		DynamicLeavesBlock leaves = TreeHelper.getLeaves(blockState);
		return (leaves != null) && this == leaves.getFamily(blockState, blockAccess, pos);
	}

	public interface IConnectable {
		boolean isConnectable(BlockState blockState);
	}

	LinkedList<IConnectable> vanillaConnectables = new LinkedList<>();

	public void addConnectableVanillaLeaves(IConnectable connectable) {
		this.vanillaConnectables.add(connectable);
	}

	public void removeConnectableVanillaLeaves(IConnectable connectable) {
		this.vanillaConnectables.remove(connectable);
	}

	public boolean isCompatibleVanillaLeaves(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {

		Block block = blockState.getBlock();

		if (!(block instanceof DynamicLeavesBlock)) {
			for (IConnectable connectable : vanillaConnectables) {
				if (connectable.isConnectable(blockState)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean isCompatibleGenericLeaves(BlockState blockState, IWorld blockAccess, BlockPos pos) {
		return isCompatibleDynamicLeaves(blockState, blockAccess, pos) || isCompatibleVanillaLeaves(blockState, blockAccess, pos);
	}

	public LeavesProperties getCommonLeaves() {
		return this.commonLeaves;
	}

	public void setCommonLeaves (LeavesProperties properties) {
		this.commonLeaves = properties;
		properties.setFamily(this);
	}

	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////

	@Override
	public String toString() {
		return "Family{registryName=" + this.getRegistryName() + "}";
	}

	@Override
	public String toLoadDataString() {
		return this.getString(Pair.of("commonLeaves", this.commonLeaves), Pair.of("maxBranchRadius", this.maxBranchRadius),
				Pair.of("hasSurfaceRoot", this.hasSurfaceRoot), Pair.of("hasStrippedBranch", this.hasStrippedBranch));
	}

	@Override
	public String toReloadDataString() {
		return this.getString(Pair.of("commonLeaves", this.commonLeaves), Pair.of("maxBranchRadius", this.maxBranchRadius),
				Pair.of("commonSpecies", this.commonSpecies), Pair.of("primitiveLog", this.primitiveLog),
				Pair.of("primitiveStrippedLog", this.primitiveStrippedLog), Pair.of("stick", this.stick),
				Pair.of("hasConiferVariants", this.hasConiferVariants), Pair.of("canSupportCocoa", this.canSupportCocoa));
	}

}