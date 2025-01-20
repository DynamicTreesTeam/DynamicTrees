package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.animation.AnimationHandler;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.cell.MetadataCell;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.util.BlockBounds;
import com.dtteam.dynamictrees.util.MutableLazyValue;
import com.dtteam.dynamictrees.util.Optionals;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.util.ResourceLocationUtils.prefix;
import static com.dtteam.dynamictrees.util.ResourceLocationUtils.suffix;

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
        public boolean onTreeActivated(TreeActivationContext context) {
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
        public Species getSpeciesForLocation(LevelAccessor level, BlockPos trunkPos) {
            return Species.NULL_SPECIES;
        }
    };

    /**
     * Central registry for all {@link Family} objects.
     */
    public static final TypedRegistry<Family> REGISTRY = new TypedRegistry<>(Family.class, NULL_FAMILY, TYPE);

    protected Species commonSpecies;

    protected LeavesProperties commonLeaves = LeavesProperties.NULL;

    //Branches
    /**
     * The dynamic branch used by this tree family
     */
    private Supplier<BranchBlock> branch;
    /**
     * The stripped variant of the branch used by this tree family
     */
    private Supplier<BranchBlock> strippedBranch;
    protected boolean hasStrippedBranch = true;
    /**
     * The minimum radius that the branch needs to have to be stripped by an axe
     * If it's not modified by a tree-pack (null), it uses the value of {@link IConfigHelper#MIN_RADIUS_FOR_STRIP}
     */
    protected Integer minRadiusForStripping = null;
    /**
     * Whether the radius of the branch is reduced by 1 when stripped.
     * This parameter is ignored if the value of {@link IConfigHelper#ENABLE_STRIP_RADIUS_REDUCTION} is set to FALSE.
     */
    protected boolean reduceRadiusWhenStripping = true;
    /**
     * The dynamic branch's block item
     */
    private Supplier<Item> branchItem;
    /**
     * The surface root used by this tree family
     */
    private Supplier<SurfaceRootBlock> surfaceRoot;
    protected boolean hasSurfaceRoot = false;
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
     * The maximum radius of a {@link BranchBlock} belonging to this family. {@link Species#getMaxBranchRadius()} will be
     * clamped to this value.
     */
    private int maxBranchRadius = BranchBlock.MAX_RADIUS;

    //Misc
    /**
     * The stick that is returned when a whole log can't be dropped
     */
    private Item stick = Items.STICK;

    protected float lootVolumeMultiplier = 1.0f;

//    @OnlyIn(Dist.CLIENT)
    public int woodRingColor; // For rooty blocks
//    @OnlyIn(Dist.CLIENT)
    public int woodBarkColor; // For rooty water

    /**
     * A list of child species, added to when tree family is set for species.
     */
    private final Set<Species> species = new HashSet<>();

    private Family() {
        this.setRegistryName(DynamicTrees.NULL);
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
        this.setBranch(this.createBranch(this.getBranchName()));
        this.setBranchItem(this.createBranchItem(this.getBranchName(), this.branch));

        if (this.hasStrippedBranch()) {
            this.setStrippedBranch(this.createBranch(this.getBranchName("stripped_")));
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

    public Species getSpeciesForLocation(LevelAccessor level, BlockPos trunkPos) {
        return this.getSpeciesForLocation(level, trunkPos, this.commonSpecies);
    }


    public Species getSpeciesForLocation(BlockGetter level, BlockPos trunkPos, Species defaultSpecies) {
        for (final Species species : this.species) {
            if (species.shouldOverrideCommon(level, trunkPos)) {
                return species;
            }
        }

        return defaultSpecies;
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    public record TreeActivationContext(Level level, BlockPos rootPos, BlockPos hitPos, BlockState hitState,
                                        Player player, InteractionHand hand, @Nullable ItemStack heldItem,
                                        BlockHitResult hitResult) { }

    public boolean onTreeActivated(TreeActivationContext context) {
        if (canStripBranch(context.hitState, context.level, context.hitPos, context.player, context.heldItem)) {
            return stripBranch(context.hitState, context.level, context.hitPos, context.player, context.heldItem);
        }

        if (context.rootPos != BlockPos.ZERO) {
            return TreeHelper.getExactSpecies(context.level, context.hitPos).onTreeActivated(context);
        }

        return false;
    }

    public boolean canStripBranch(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        BranchBlock branchBlock = TreeHelper.getBranch(state);
        if (branchBlock == null) {
            return false;
        }
        return branchBlock.canBeStripped(state, level, pos, player, heldItem);
    }

    public boolean stripBranch(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        if (this.hasStrippedBranch()) {
            this.getBranch().ifPresent(branch -> {
                branch.stripBranch(state, level, pos, player, heldItem);
                if (level.isClientSide) {
                    level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    Services.COMPAT.invalidateWailaPosition();
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

    protected ResourceLocation getBranchName() {
        return getBranchName("");
    }

    protected ResourceLocation getBranchName(final String prefix) {
        return prefix(this.getRegistryName(), prefix);
    }

    protected String getBranchNameSuffix() {
        return BranchBlock.NAME_SUFFIX;
    }

    /**
     * Instantiates and sets up the actual {@link BranchBlock} object. Can be overridden by sub-classes for custom
     * branch blocks.
     *
     * @return The instantiated {@link BranchBlock}.
     */
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(name, this.getProperties()) :
                new BasicBranchBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }

    /**
     * Creates branch block and adds it to the relevant {@link RegistryHandler}.
     *
     * @param name The {@link ResourceLocation} registry name.
     * @return The created {@link BranchBlock}.
     */
    protected Supplier<BranchBlock> createBranch(final ResourceLocation name) {
        return RegistryHandler.addBlock(suffix(name, getBranchNameSuffix()), () -> createBranchBlock(name));
    }

    /**
     * Creates and registers a {@link BlockItem} for the given branch with the given registry name.
     *
     * @param registryName The {@link ResourceLocation} registry name for the item.
     * @param branchSup    A supplier for the {@link BranchBlock} to create the {@link BlockItem} for.
     * @return A supplier for the {@link BlockItem}.
     */
    public Supplier<BlockItem> createBranchItem(final ResourceLocation registryName, final Supplier<BranchBlock> branchSup) {
        return RegistryHandler.addItem(suffix(registryName, getBranchNameSuffix()), () -> new BlockItem(branchSup.get(), new Item.Properties()));
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
     *
     * @param level   The level the tree is generating in
     * @param species The species of the tree generated
     * @param pos     The position of the branch block
     * @return branch block picked
     */
    public Optional<BranchBlock> getBranchForPlacement(LevelAccessor level, Species species, BlockPos pos) {
        return getBranch();
    }

    /**
     * This is used for trees with root systems, i.e. mangrove trees.
     * By default, most trees do not have one, so we just return the normal branch.
     *
     * @param level   the world
     * @param species the species
     * @param pos     the position the branch will be placed on
     * @return the branch block selected
     */
    public Optional<BranchBlock> getBranchForRootsPlacement(LevelAccessor level, Species species, BlockPos pos) {
        return getBranch();
    }

    public Optional<BranchBlock> getStrippedBranch() {
        return Optionals.ofBlock(this.strippedBranch);
    }

    public Optional<Item> getBranchItem() {
        return Optionals.ofItem(this.branchItem);
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

//    @OnlyIn(Dist.CLIENT)
    public int getRootColor(BlockState state, boolean getBark) {
        return getBark ? woodBarkColor : woodRingColor;
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

    public List<ItemStack> getLogDropsForBranch(float volume, int branch) {
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

    public MapColor getDefaultBranchMapColor() {
        return MapColor.WOOD;
    }

    public boolean getDefaultFlammable() {
        return true;
    }

    public SoundType getDefaultBranchSoundType() {
        return SoundType.WOOD;
    }

    public BlockBehaviour.Properties getDefaultBranchProperties(MapColor mapColor) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().sound(this.getDefaultBranchSoundType()).mapColor(mapColor)
                .noLootTable().requiresCorrectToolForDrops();
        if (!this.isFireProof())
            properties.ignitedByLava();
        return properties;
    }

    private BlockBehaviour.Properties properties;

    /**
     * Gets the {@link #properties} for this {@link Family} object.
     *
     * @return The {@link #properties} for this {@link Family} object.
     */
    public BlockBehaviour.Properties getProperties() {
        return this.properties == null ? this.getDefaultBranchProperties(this.getDefaultBranchMapColor()) : this.properties;
    }


    public Family setProperties(BlockBehaviour.Properties properties) {
        this.properties = properties;
        return this;
    }

    public float getLootVolumeMultiplier() {
        return lootVolumeMultiplier;
    }

    public void setLootVolumeMultiplier(float lootVolumeMultiplier) {
        this.lootVolumeMultiplier = lootVolumeMultiplier;
    }

    ///////////////////////////////////////////
    //BRANCHES
    ///////////////////////////////////////////

    public int getRadiusForCellKit(BlockGetter blockAccess, BlockPos pos, BlockState blockState, Direction dir, BranchBlock branch) {
        int radius = branch.getRadius(blockState);
        int meta = MetadataCell.NONE;
        if (radius == getPrimaryThickness()) {
            if (blockAccess.getBlockState(pos.below()).getBlock() == branch) {
                meta = MetadataCell.TOP_BRANCH;
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
     * Thickness of tips of the root system.
     * By default, most trees do not have one, so we return the regular primary thickness.
     */
    public int getPrimaryRootThickness() {
        return primaryThickness;
    }

    /**
     * Thickness of the branch connected to a twig (radius == getPrimaryThickness) [default = 2]
     */
    public int getSecondaryThickness() {
        return secondaryThickness;
    }

    /**
     * Thickness of the root connected to tips in the root system.
     * By default, most trees do not have one, so we return the regular secondary thickness.
     */
    public int getSecondaryRootThickness() {
        return secondaryThickness;
    }

    public boolean hasStrippedBranch() {
        return this.hasStrippedBranch;
    }

    public void setHasStrippedBranch(boolean hasStrippedBranch) {
        this.hasStrippedBranch = hasStrippedBranch;
    }

    public int getMinRadiusForStripping() {
        if (minRadiusForStripping == null) return Services.CONFIG.getIntConfig(IConfigHelper.MIN_RADIUS_FOR_STRIP);
        return minRadiusForStripping;
    }

    public void setMinRadiusForStripping(int radius) {
        this.minRadiusForStripping = radius;
    }

    public boolean reduceRadiusWhenStripping() {
        if (Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_STRIP_RADIUS_REDUCTION))
            return reduceRadiusWhenStripping;
        return false;
    }

    public void setReduceRadiusWhenStripping(boolean reduceRadiusWhenStripping) {
        this.reduceRadiusWhenStripping = reduceRadiusWhenStripping;
    }

    public void addValidBranches(BranchBlock... branches) {
        this.validBranches.addAll(Arrays.asList(branches));
    }

    public int getBranchBlockIndex(BranchBlock block) {
        int index = this.validBranches.indexOf(block);
        if (index < 0) {
            DynamicTrees.LOG.warn("Block {} not valid branch for {}.", block, this);
            return 0;
        }
        return index;
    }

    @Nullable
    public BranchBlock getValidBranchBlock(int index) {
        if (index < validBranches.size())
            return this.validBranches.get(index);
        else {
            DynamicTrees.LOG.warn("Attempted to get branch block of index {} but {} only has {} valid branches.", index, this, validBranches.size());
            return this.validBranches.getFirst();
        }
    }

    public boolean isValidBranchBlock(BranchBlock block) {
        return this.validBranches.contains(block);
    }

    public int getNumberOfValidBranchBlocks() {
        return validBranches.size();
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
    // ROOT SYSTEM
    ///////////////////////////////////////////

    //By default there is no root species anyways. This is overriden by families like mangrove.
    public boolean isAcceptableSoilForRootSystem(BlockState soilBlockState) {
        return getCommonSpecies().isAcceptableSoil(soilBlockState);
    }

    public boolean hasRootSystem(){
        return false;
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

//    public void addGeneratedBlockTags (Function<TagKey<Block>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block>> tagAppender){
//        getBranch().ifPresent(branch -> {
//            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
//            defaultBranchTags().forEach(tag -> {
//                if (!isOnlyIfLoaded()) {
//                    tagAppender.apply(tag).add(branch);
//                } else {
//                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(branch));
//                }
//            });
//        });
//
//        // Create stripped branch tag and harvest tag if the family has a stripped branch.
//        getStrippedBranch().ifPresent(strippedBranch -> {
//            tierTag(getDefaultStrippedBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(strippedBranch));
//            defaultStrippedBranchTags().forEach(tag ->
//            {
//                if (!isOnlyIfLoaded()) {
//                    tagAppender.apply(tag).add(strippedBranch);
//                } else {
//                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(strippedBranch));
//                }
//            });
//        });
//    }
//
//    protected Optional<IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block>> tierTag(@Nullable Tier tier, Function<TagKey<Block>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block>> tagAppender) {
//        if (tier == null)
//            return Optional.empty();
//
//        TagKey<Block> tag = tier.getTag();
//
//        return tag == null ? Optional.empty() : Optional.of(tagAppender.apply(tag));
//    }
//
//    public void addGeneratedItemTags (Function<TagKey<Item>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> tagAppender){
//        getBranchItem().ifPresent(item -> {
//                    if (!isOnlyIfLoaded()) {
//                        defaultBranchItemTags().forEach(tag -> tagAppender.apply(tag).add(item));
//                    } else {
//                        defaultBranchItemTags().forEach(tag -> tagAppender.apply(tag).addOptional(BuiltInRegistries.ITEM.getKey(item)));
//                    }
//                }
//        );
//    }
//
//    /**
//     * @return a constructor for the relevant branch block model builder for the corresponding loader
//     */
//    public BiFunction<BlockModelBuilder, FileHelper, BranchLoaderBuilder> getBranchLoaderConstructor() {
//        return BranchLoaderBuilder::branch;
//    }

//    protected final MutableLazyValue<Generator<DTDataProvider, Family>> branchStateGenerator =
//            MutableLazyValue.supplied(BranchStateGenerator::new);
//
//    protected final MutableLazyValue<Generator<DTDataProvider, Family>> strippedBranchStateGenerator =
//            MutableLazyValue.supplied(StrippedBranchStateGenerator::new);
//
//    protected final MutableLazyValue<Generator<? extends DTDataProvider, Family>> surfaceRootStateGenerator =
//            MutableLazyValue.supplied(SurfaceRootStateGenerator::new);
//
//    @Override
//    public void generateStateData(DataProvider provider) {
//        // Generate branch block state and model.
//        this.branchStateGenerator.get().generate(provider, this);
//        this.strippedBranchStateGenerator.get().generate(provider, this);
//
//        // Generate surface root block state and model.
//        this.surfaceRootStateGenerator.get().generate(provider, this);
//    }

//    public ResourceLocation getBranchItemParentLocation() {
//        return DynamicTrees.location("item/branch");
//    }
//
//    public ResourceLocation getRootItemParentLocation() {
//        return DynamicTrees.location("item/root_branch");
//    }
//
//    protected final MutableLazyValue<Generator<DTItemModelProvider, Family>> branchItemModelGenerator =
//            MutableLazyValue.supplied(BranchItemModelGenerator::new);
//
//    protected final MutableLazyValue<Generator<DTLangProvider, Family>> familyLangGenerator =
//            MutableLazyValue.supplied(FamilyLangGenerator::new);
//
//    @Override
//    public void generateItemModelData(DTItemModelProvider provider) {
//        // Generate branch item models.
//        this.branchItemModelGenerator.get().generate(provider, this);
//    }
//
//    @Override
//    public void generateLangData(DTLangProvider provider) {
//        this.familyLangGenerator.get().generate(provider, this);
//    }

    protected List<String> onlyIfLoaded = new ArrayList<>();
    //Texture overrides
    protected HashMap<String, ResourceLocation> textureOverrides = new HashMap<>();
    protected HashMap<String, ResourceLocation> modelOverrides = new HashMap<>();
    protected HashMap<String, String> langOverrides = new HashMap<>();
    public static final String BRANCH = "branch";
    public static final String BRANCH_TOP = "branch_top";
    public static final String STRIPPED_BRANCH = "stripped_branch";
    public static final String STRIPPED_BRANCH_TOP = "stripped_branch_top";
    public static final String ROOTS_SIDE = "roots_side";
    public static final String ROOTS_TOP = "roots_top";
    public static final String COVERED_ROOTS_BLOCK = "covered_roots_block";


    public void setOnlyIfLoaded(String onlyIfLoaded) {
        this.onlyIfLoaded.add(onlyIfLoaded);
    }

    public boolean isOnlyIfLoaded() {
        return !onlyIfLoaded.isEmpty();
    }

    public void setTextureOverrides(Map<String, ResourceLocation> textureOverrides) {
        this.textureOverrides.putAll(textureOverrides);
    }

    public Optional<ResourceLocation> getTexturePath(String key) {
        return Optional.ofNullable(textureOverrides.getOrDefault(key, null));
    }

    public void setModelOverrides(Map<String, ResourceLocation> modelOverrides) {
        this.modelOverrides.putAll(modelOverrides);
    }

    public Optional<ResourceLocation> getModelPath(String key) {
        return Optional.ofNullable(modelOverrides.getOrDefault(key, null));
    }

    public void setLangOverrides(Map<String, String> langOverrides) {
        this.langOverrides.putAll(langOverrides);
    }

    public Optional<String> getLangOverride(String key) {
        return Optional.ofNullable(langOverrides.getOrDefault(key, null));
    }

    public void addBranchTextures(BiConsumer<String, ResourceLocation> textureConsumer, ResourceLocation primitiveLogLocation, Block sourceBlock) {
        ResourceLocation bark = primitiveLogLocation;
        ResourceLocation rings = suffix(primitiveLogLocation, "_top");

        AtomicBoolean isStripped = new AtomicBoolean(false);
        getPrimitiveStrippedLog().ifPresent(l -> isStripped.set(l.equals(sourceBlock)));
        if (isStripped.get()) {
            if (textureOverrides.containsKey(STRIPPED_BRANCH)) bark = textureOverrides.get(STRIPPED_BRANCH);
            if (textureOverrides.containsKey(STRIPPED_BRANCH_TOP)) rings = textureOverrides.get(STRIPPED_BRANCH_TOP);
        } else {
            if (textureOverrides.containsKey(BRANCH)) bark = textureOverrides.get(BRANCH);
            if (textureOverrides.containsKey(BRANCH_TOP)) rings = textureOverrides.get(BRANCH_TOP);
        }

        textureConsumer.accept("bark", bark);
        textureConsumer.accept("rings", rings);
    }

    public void addRootTextures(BiConsumer<String, ResourceLocation> textureConsumer, ResourceLocation primitiveLogLocation) {
        ResourceLocation bark = suffix(primitiveLogLocation, "_side");
        ResourceLocation rings = suffix(primitiveLogLocation, "_top");

        if (textureOverrides.containsKey(ROOTS_SIDE)) bark = textureOverrides.get(ROOTS_SIDE);
        if (textureOverrides.containsKey(ROOTS_TOP)) rings = textureOverrides.get(ROOTS_TOP);

        textureConsumer.accept("bark", bark);
        textureConsumer.accept("rings", rings);
    }

    //////////////////////////////
    // JAVA OBJECT STUFF
    //////////////////////////////

    @Override
    public String toLoadDataString() {
        return this.getString(
                Pair.of("commonLeaves", this.commonLeaves),
                Pair.of("maxBranchRadius", this.maxBranchRadius),
                Pair.of("hasSurfaceRoot", this.hasSurfaceRoot),
                Pair.of("hasStrippedBranch", this.hasStrippedBranch)
        );
    }

    @Override
    public String toReloadDataString() {
        return this.getString(
                Pair.of("commonLeaves", this.commonLeaves),
                Pair.of("maxBranchRadius", this.maxBranchRadius),
                Pair.of("commonSpecies", this.commonSpecies),
                Pair.of("primitiveLog", this.primitiveLog),
                Pair.of("primitiveStrippedLog", this.primitiveStrippedLog),
                Pair.of("stick", this.stick),
                Pair.of("minRadiusForStrip", this.minRadiusForStripping)
        );
    }
}