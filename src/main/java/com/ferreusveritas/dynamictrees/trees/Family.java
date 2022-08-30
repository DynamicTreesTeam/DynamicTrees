package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.data.*;
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
import com.ferreusveritas.dynamictrees.compat.waila.WailaOther;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandler;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.MutableLazyValue;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;
import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.suffix;

/**
 * This structure describes a Family whose member Species all have a common branch.
 * <p>
 * A {@link Family} is more or less just a definition of {@link BranchBlock} blocks. It also defines the cellular
 * automata function of the {@link BranchBlock}.  It defines the type of wood that the tree is made of and consequently
 * what kind of log you get when you cut it down.
 * <p>
 * A DynamicTree does not contain a reference to a Seed, Leaves, Sapling, or how it should grow(how fast, how tall,
 * etc). It does not control what drops it produces or what fruit it grows.  It does not control where it should grow.
 * All of these capabilities lie in the Species class for which a DynamicTree should always contain one default
 * species(the common species).
 *
 * @author ferreusveritas
 */
public class Family extends RegistryEntry<Family> implements Resettable<Family> {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(Family::new);

    public final static Family NULL_FAMILY = new Family() {
        @Override
        public void setupCommonSpecies(Species species) {
        }

        @Override
        public Species getCommonSpecies() {
            return Species.NULL_SPECIES;
        }

        @Override
        public boolean onTreeActivated(Level world, BlockPos hitPos, BlockState state, Player player, InteractionHand hand, ItemStack heldItem, BlockHitResult hit) {
            return false;
        }

        @Override
        public ItemStack getStick(int qty) {
            return ItemStack.EMPTY;
        }

        @Override
        public BranchBlock getValidBranchBlock(int index) {
            return null;
        }

        @Override
        public Species getSpeciesForLocation(LevelAccessor world, BlockPos trunkPos) {
            return Species.NULL_SPECIES;
        }
    };

    /**
     * Central registry for all {@link Family} objects.
     */
    public static final TypedRegistry<Family> REGISTRY = new TypedRegistry<>(Family.class, NULL_FAMILY, TYPE);

    protected Species commonSpecies;

    protected LeavesProperties commonLeaves = LeavesProperties.NULL_PROPERTIES;

    //Branches
    /**
     * The dynamic branch used by this tree family
     */
    private Supplier<BranchBlock> branch;
    /**
     * The stripped variant of the branch used by this tree family
     */
    private Supplier<BranchBlock> strippedBranch;
    /**
     * The dynamic branch's block item
     */
    private Supplier<Item> branchItem;
    /**
     * The surface root used by this tree family
     */
    private Supplier<SurfaceRootBlock> surfaceRoot;
    /**
     * The primitive (vanilla) log to base the texture, drops, and other behavior from
     */
    private Block primitiveLog = Blocks.AIR;
    /**
     * The primitive stripped log to base the texture, drops, and other behavior from
     */
    private Block primitiveStrippedLog = Blocks.AIR;

    /**
     * A list of branches the tree accepts as its own. Used for the falling tree renderer
     */
    private final List<BranchBlock> validBranches = new LinkedList<>();

    /**
     * The maximum radius of a {@link BranchBlock} belonging to this family. {@link Species#maxBranchRadius} will be
     * clamped to this value.
     */
    private int maxBranchRadius = BranchBlock.MAX_RADIUS;

    //Leaves
    /**
     * Used to modify the getRadiusForCellKit call to create a special case
     */
    protected boolean hasConiferVariants = false;

    protected boolean hasSurfaceRoot = false;
    protected boolean hasStrippedBranch = true;

    //Misc
    /**
     * The stick that is returned when a whole log can't be dropped
     */
    private Item stick = Items.STICK;
    /**
     * Weather the branch can support cocoa pods on it's surface [default = false]
     */
    public boolean canSupportCocoa = false;

    @OnlyIn(Dist.CLIENT)
    public int woodRingColor; // For rooty blocks
    @OnlyIn(Dist.CLIENT)
    public int woodBarkColor; // For rooty water

    /**
     * A list of child species, added to when tree family is set for species.
     */
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

    public Family addSpecies(final Species species) {
        this.species.add(species);
        return this;
    }

    public Set<Species> getSpecies() {
        return this.species;
    }

    ///////////////////////////////////////////
    // SPECIES LOCATION OVERRIDES
    ///////////////////////////////////////////

//
    public Species getSpeciesForLocation(LevelAccessor world, BlockPos trunkPos) {
        return this.getSpeciesForLocation(world, trunkPos, this.commonSpecies);
    }


    public Species getSpeciesForLocation(BlockGetter world, BlockPos trunkPos, Species defaultSpecies) {
        for (final Species species : this.species) {
            if (species.shouldOverrideCommon(world, trunkPos)) {
                return species;
            }
        }

        return defaultSpecies;
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    public boolean onTreeActivated(Level world, BlockPos hitPos, BlockState state, Player player, InteractionHand hand, @Nullable ItemStack heldItem, BlockHitResult hit) {

        if (this.canSupportCocoa) {
            BlockPos pos = hit.getBlockPos();
            if (heldItem != null) {
                if (heldItem.getItem() == Items.COCOA_BEANS) {
                    BranchBlock branch = TreeHelper.getBranch(state);
                    if (branch != null && branch.getRadius(state) == 8) {
                        if (hit.getDirection() != Direction.UP && hit.getDirection() != Direction.DOWN) {
                            pos = pos.relative(hit.getDirection());
                        }
                        if (world.isEmptyBlock(pos)) {
                            BlockState cocoaState = DTRegistries.COCOA_FRUIT.get().getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, hit)));
                            assert cocoaState != null;
                            Direction facing = cocoaState.getValue(HorizontalDirectionalBlock.FACING);
                            world.setBlock(pos, DTRegistries.COCOA_FRUIT.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing), 2);
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

        if (canStripBranch(state, world, hitPos, player, heldItem)) {
            return stripBranch(state, world, hitPos, player, heldItem);
        }

        if (rootPos != BlockPos.ZERO) {
            return TreeHelper.getExactSpecies(world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, hit);
        }

        return false;
    }

    public boolean canStripBranch(BlockState state, Level world, BlockPos pos, Player player, ItemStack heldItem) {
        BranchBlock branchBlock = TreeHelper.getBranch(state);
		if (branchBlock == null) {
			return false;
		}
        return branchBlock.canBeStripped(state, world, pos, player, heldItem);
    }

    public boolean stripBranch(BlockState state, Level world, BlockPos pos, Player player, ItemStack heldItem) {
        if (this.hasStrippedBranch()) {
            this.getBranch().ifPresent(branch -> {
                branch.stripBranch(state, world, pos, player, heldItem);
                if (world.isClientSide) {
                    world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    WailaOther.invalidateWailaPosition();
                }
            });
			return this.getBranch().isPresent();
		} else {
			return false;
		}
    }


    ///////////////////////////////////////////
    // TREE PROPERTIES
    ///////////////////////////////////////////

    public boolean isWood() {
        return true;
    }

    /**
     * Creates the branch block. Can be overridden by sub-classes who want full control over registry and instantiation
     * of the branch.
     *
     * @return A supplier for the {@link BranchBlock}.
     */
    public Supplier<BranchBlock> createBranch() {
        return this.createBranch(this.getBranchRegName(""));
    }

    /**
     * Gets a branch name with the given prefix and <tt>_branch</tt> as the suffix.
     *
     * @param prefix The prefix.
     * @return The {@link ResourceLocation} registry name for the branch.
     */
    protected ResourceLocation getBranchRegName(final String prefix) {
        return suffix(prefix(this.getRegistryName(), prefix), "_branch");
    }

    /**
     * Instantiates and sets up the actual {@link BranchBlock} object. Can be overridden by sub-classes for custom
     * branch blocks.
     *
     * @return The instantiated {@link BranchBlock}.
     */
    protected BranchBlock createBranchBlock() {
        final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(this.getProperties()) : new BasicBranchBlock(this.getProperties());
		if (this.isFireProof()) {
			branch.setFireSpreadSpeed(0).setFlammability(0);
		}
        return branch;
    }

    /**
     * Creates branch block and adds it to the relevant {@link RegistryHandler}.
     *
     * @param registryName The {@link ResourceLocation} registry name.
     * @return A supplier for the {@link BranchBlock}.
     */
    protected Supplier<BranchBlock> createBranch(final ResourceLocation registryName) {
        return RegistryHandler.addBlock(registryName, this::createBranchBlock);
    }

    /**
     * Creates and registers a {@link BlockItem} for the given branch with the given registry name.
     *
     * @param registryName The {@link ResourceLocation} registry name for the item.
     * @param branchSup    A supplier for the {@link BranchBlock} to create the {@link BlockItem} for.
     * @return A supplier for the {@link BlockItem}.
     */
    public Supplier<BlockItem> createBranchItem(final ResourceLocation registryName, final Supplier<BranchBlock> branchSup) {
        return RegistryHandler.addItem(registryName, () -> new BlockItem(branchSup.get(), new Item.Properties()));
    }

    protected Family setBranch(final Supplier<BranchBlock> branchSup) {
        this.branch = this.setupBranch(branchSup, this.hasStrippedBranch);
        return this;
    }

    protected Family setStrippedBranch(final Supplier<BranchBlock> branch) {
        this.strippedBranch = this.setupBranch(branch, false);
        return this;
    }

    protected Supplier<BranchBlock> setupBranch(final Supplier<BranchBlock> branchBlockSup, final boolean canBeStripped) {
        return () -> {
            BranchBlock branchBlock = branchBlockSup.get();
            branchBlock.setFamily(this); // Link the branch to the tree.
            branchBlock.setCanBeStripped(canBeStripped);
            this.addValidBranches(branchBlock); // Add the branch as a valid branch.
            return branchBlock;
        };
    }

    @SuppressWarnings("unchecked")
    protected <T extends Item> Family setBranchItem(Supplier<T> branchItemSup) {
        this.branchItem = (Supplier<Item>) branchItemSup;
        return this;
    }

    public Optional<BranchBlock> getBranch() {
        return Optionals.ofBlock(this.branch);
    }
    /**
     * Version of getBranch() used by jocodes to generate the tree.
     * By default it acts just like getBranch() but it can be overriden
     * by addons to customize the branch selected by the jocode
     * @param world     The world the tree is generating in
     * @param species   The species of the tree generated
     * @param pos       The position of the branch block
     * @return branch block picked
     */
    public Optional<BranchBlock> getBranchForPlacement(LevelAccessor world, Species species, BlockPos pos) {
        return getBranch();
    }

    public Optional<BranchBlock> getStrippedBranch() {
        return Optionals.ofBlock(strippedBranch);
    }

    public Optional<Item> getBranchItem() {
        return Optionals.ofItem(branchItem.get());
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
        return getBark ? woodBarkColor : woodRingColor;
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
    public Family setStick(Item item) {
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
        return this.stick == Items.AIR ? ItemStack.EMPTY : new ItemStack(this.stick, Mth.clamp(qty, 0, 64));
    }

    public void setCanSupportCocoa(boolean canSupportCocoa) {
        this.canSupportCocoa = canSupportCocoa;
    }

    /**
     * Used to set the type of log item that a tree drops when it's harvested. Use this function to explicitly set the
     * itemstack instead of having it done automatically.
     *
     * @param primitiveLog A block object that is the log
     * @param primitiveLog An itemStack of the log item
     * @return {@link Family} for chaining calls
     */
    public Family setPrimitiveLog(Block primitiveLog) {
        this.primitiveLog = primitiveLog;

		if (this.branch != null) {
			this.branch.get().setPrimitiveLogDrops(new ItemStack(primitiveLog));
		}

        return this;
    }

    public Family setPrimitiveStrippedLog(Block primitiveStrippedLog) {
        this.primitiveStrippedLog = primitiveStrippedLog;

		if (this.strippedBranch != null) {
			this.strippedBranch.get().setPrimitiveLogDrops(new ItemStack(primitiveStrippedLog));
		}

        return this;
    }

    /**
     * Gets the primitive full block (vanilla)log that represents this tree's material. Chiefly used to determine the
     * wood hardness for harvesting behavior.
     *
     * @return Block of the primitive log.
     */
    public Optional<Block> getPrimitiveLog() {
        return Optionals.ofBlock(primitiveLog);
    }

    public Optional<Block> getPrimitiveStrippedLog() {
        return Optionals.ofBlock(primitiveStrippedLog);
    }

    private List<ItemStack> getLogDropsForBranch(float volume, int branch) {
        BranchBlock branchBlock = getValidBranchBlock(branch);
        List<ItemStack> logs = new LinkedList<>();
        if (branchBlock != null) {
            branchBlock.getPrimitiveLogs(volume, logs);
        }
        return logs;
    }

    private boolean isFireProof = false;

    public boolean isFireProof() {
        return isFireProof;
    }

    public void setIsFireProof(boolean isFireProof) {
        this.isFireProof = isFireProof;
    }

    public SoundType getBranchSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return this.getDefaultBranchSoundType();
    }

    /**
     * {@code null} = can harvest with hand
     */
    @Nullable
    public Tier getDefaultBranchHarvestTier() {
        return null;
    }

    /**
     * {@code null} = can harvest with hand
     */
    @Nullable
    public Tier getDefaultStrippedBranchHarvestTier() {
        return null;
    }

    public Material getDefaultBranchMaterial() {
        return Material.WOOD;
    }

    public SoundType getDefaultBranchSoundType() {
        return SoundType.WOOD;
    }

    public BlockBehaviour.Properties getDefaultBranchProperties(final Material material, final MaterialColor materialColor) {
        return BlockBehaviour.Properties.of(material, materialColor).sound(this.getDefaultBranchSoundType()).noLootTable().requiresCorrectToolForDrops();
    }

    private BlockBehaviour.Properties properties;

    /**
     * Gets the {@link #properties} for this {@link Family} object.
     *
     * @return The {@link #properties} for this {@link Family} object.
     */
    public BlockBehaviour.Properties getProperties() {
        return this.properties == null ? this.getDefaultBranchProperties(this.getDefaultBranchMaterial(),
                this.getDefaultBranchMaterial().getColor()) : this.properties;
    }


    public Family setProperties(BlockBehaviour.Properties properties) {
        this.properties = properties;
        return this;
    }

    ///////////////////////////////////////////
    //BRANCHES
    ///////////////////////////////////////////

    public int getRadiusForCellKit(BlockGetter blockAccess, BlockPos pos, BlockState blockState, Direction dir, BranchBlock branch) {
        int radius = branch.getRadius(blockState);
        int meta = MetadataCell.NONE;
        if (hasConiferVariants && radius == getPrimaryThickness()) {
            if (blockAccess.getBlockState(pos.below()).getBlock() == branch) {
                meta = MetadataCell.CONIFERTOP;
            }
        }

        return MetadataCell.radiusAndMeta(radius, meta);
    }

    private int primaryThickness = 1;
    private int secondaryThickness = 2;

    public void setPrimaryThickness(int primaryThickness) {
        this.primaryThickness = primaryThickness;
    }

    public void setSecondaryThickness(int secondaryThickness) {
        this.secondaryThickness = secondaryThickness;
    }

    /**
     * Thickness of a twig [default = 1]
     */
    public int getPrimaryThickness() {
        return primaryThickness;
    }

    /**
     * Thickness of the branch connected to a twig (radius == getPrimaryThickness) [default = 2]
     */
    public int getSecondaryThickness() {
        return secondaryThickness;
    }

    public boolean hasStrippedBranch() {
        return this.hasStrippedBranch;
    }

    public void setHasStrippedBranch(boolean hasStrippedBranch) {
        this.hasStrippedBranch = hasStrippedBranch;
    }

    public void addValidBranches(BranchBlock... branches) {
        this.validBranches.addAll(Arrays.asList(branches));
    }

    public int getBranchBlockIndex(BranchBlock block) {
        int index = this.validBranches.indexOf(block);
        if (index < 0) {
            LogManager.getLogger().warn("Block {} not valid branch for {}.", block, this);
            return 0;
        }
        return index;
    }

    @Nullable
    public BranchBlock getValidBranchBlock(int index) {
        if (index < validBranches.size())
            return this.validBranches.get(index);
        else {
            LogManager.getLogger().warn("Attempted to get branch block of index {} but {} only has {} valid branches.", index, this, validBranches.size());
            return this.validBranches.get(0);
        }
    }

    //Useful for addons
    public boolean isValidBranchBlock (BranchBlock block){
        return this.validBranches.contains(block);
    }

    private boolean branchIsLadder = true;

    public void setBranchIsLadder(boolean branchIsLadder) {
        this.branchIsLadder = branchIsLadder;
    }

    public boolean branchIsLadder() {
        return branchIsLadder;
    }

    private int maxSignalDepth = 32;

    public int getMaxSignalDepth() {
        return maxSignalDepth;
    }

    public void setMaxSignalDepth(int maxSignalDepth) {
        this.maxSignalDepth = maxSignalDepth;
    }

    ///////////////////////////////////////////
    // SURFACE ROOTS
    ///////////////////////////////////////////

    public boolean hasSurfaceRoot() {
        return this.hasSurfaceRoot;
    }

    public void setHasSurfaceRoot(boolean hasSurfaceRoot) {
        this.hasSurfaceRoot = hasSurfaceRoot;
    }

    public Supplier<SurfaceRootBlock> createSurfaceRoot() {
        return RegistryHandler.addBlock(suffix(this.getRegistryName(), "_root"), () -> new SurfaceRootBlock(this));
    }

    public Optional<SurfaceRootBlock> getSurfaceRoot() {
        return Optionals.ofBlock(this.surfaceRoot);
    }

    protected Family setSurfaceRoot(Supplier<SurfaceRootBlock> surfaceRootSup) {
        this.surfaceRoot = surfaceRootSup;
        return this;
    }

    ///////////////////////////////////////////
    // FALL ANIMATION HANDLING
    ///////////////////////////////////////////

    public AnimationHandler selectAnimationHandler(FallingTreeEntity fallingEntity) {
        return fallingEntity.defaultAnimationHandler();
    }

    ///////////////////////////////////////////
    // LEAVES HANDLING
    ///////////////////////////////////////////

    /**
     * When destroying leaves, an area is created from the branch endpoints to look for leaves blocks and destroy them.
     * This area is then expanded by a certain size to make sure it covers all the leaves in the canopy.
     *
     * @return the expanded block bounds.
     */
    public BlockBounds expandLeavesBlockBounds(BlockBounds bounds) {
        return bounds.expand(3);
    }

    public boolean isCompatibleDynamicLeaves(Species species, BlockState blockState, BlockGetter blockAccess, BlockPos pos) {
        final DynamicLeavesBlock leaves = TreeHelper.getLeaves(blockState);
        return (leaves != null) && (this == leaves.getFamily(blockState, blockAccess, pos)
                || species.isValidLeafBlock(leaves));
    }

    public boolean isCompatibleGenericLeaves(final Species species, BlockState blockState, LevelAccessor blockAccess, BlockPos pos) {
        return this.isCompatibleDynamicLeaves(species, blockState, blockAccess, pos);
    }

    public LeavesProperties getCommonLeaves() {
        return this.commonLeaves;
    }

    public void setCommonLeaves(LeavesProperties properties) {
        this.commonLeaves = properties;
        properties.setFamily(this);
    }

    public List<TagKey<Block>> defaultBranchTags() {
        return this.isFireProof ? Collections.singletonList(DTBlockTags.BRANCHES) :
                Collections.singletonList(DTBlockTags.BRANCHES_THAT_BURN);
    }

    public List<TagKey<Item>> defaultBranchItemTags() {
        return this.isFireProof ? Collections.singletonList(DTItemTags.BRANCHES) :
                Collections.singletonList(DTItemTags.BRANCHES_THAT_BURN);
    }

    public List<TagKey<Block>> defaultStrippedBranchTags() {
        return this.isFireProof ? Collections.singletonList(DTBlockTags.STRIPPED_BRANCHES) :
                Collections.singletonList(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);
    }

    /**
     * @return a constructor for the relevant branch block model builder for the corresponding loader
     */
    public BiFunction<BlockModelBuilder, ExistingFileHelper, BranchLoaderBuilder> getBranchLoaderConstructor() {
        return BranchLoaderBuilder::branch;
    }

    protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> branchStateGenerator =
            MutableLazyValue.supplied(BranchStateGenerator::new);

    protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> strippedBranchStateGenerator =
            MutableLazyValue.supplied(StrippedBranchStateGenerator::new);

    protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> surfaceRootStateGenerator =
            MutableLazyValue.supplied(SurfaceRootStateGenerator::new);

    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        // Generate branch block state and model.
        this.branchStateGenerator.get().generate(provider, this);
        this.strippedBranchStateGenerator.get().generate(provider, this);

        // Generate surface root block state and model.
        this.surfaceRootStateGenerator.get().generate(provider, this);
    }

    public ResourceLocation getBranchItemParentLocation() {
        return DynamicTrees.resLoc("item/branch");
    }

    protected final MutableLazyValue<Generator<DTItemModelProvider, Family>> branchItemModelGenerator =
            MutableLazyValue.supplied(BranchItemModelGenerator::new);

    public void addBranchTextures(BiConsumer<String, ResourceLocation> textureConsumer, ResourceLocation primitiveLogLocation) {
        textureConsumer.accept("bark", primitiveLogLocation);
        textureConsumer.accept("rings", suffix(primitiveLogLocation, "_top"));
    }

    @Override
    public void generateItemModelData(DTItemModelProvider provider) {
        // Generate branch item models.
        this.branchItemModelGenerator.get().generate(provider, this);
    }

    //////////////////////////////
    // JAVA OBJECT STUFF
    //////////////////////////////

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