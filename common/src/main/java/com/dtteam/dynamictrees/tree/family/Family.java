package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.block.branch.*;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.compat.WailaHelper;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.entity.animation.AnimationHandler;
import com.dtteam.dynamictrees.systems.cell.MetadataCell;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.utility.Optionals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.suffix;
import static com.dtteam.dynamictrees.utility.IdentifierUtils.surround;

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

    public static final Codec<Family> CODEC = Identifier.CODEC.comapFlatMap(Family::read, Family::getRegistryName);

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(Family::new);

    public final static Family NULL_FAMILY = new Family() {
        @Override
        public void setCommonSpecies(Species species) {
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
        public ItemStack getStickStack(int qty) {
            return ItemStack.EMPTY;
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
    protected List<BranchEntry> branches = new ArrayList<>();
    public static final int BRANCH_INDEX = 0;
    public static final int STRIPPED_BRANCH_INDEX = 1;

    protected BlockBehaviour.Properties branchProperties;

    protected boolean hasStrippedBranch = true;
    /**
     * The minimum radius that the branch needs to have to be stripped by an axe
     * If it's not modified by a tree-pack (null), it uses the value of {@link DTConfigs#minRadiusForStrip}
     */
    protected Integer minRadiusForStripping = null;
    /**
     * Whether the radius of the branch is reduced by 1 when stripped.
     * This parameter is ignored if the value of {@link DTConfigs#enableStripRadiusReduction} is set to FALSE.
     */
    protected boolean reduceRadiusWhenStripping = true;

    /**
     * The surface root used by this tree family
     */
    private Supplier<SurfaceRootBlock> surfaceRoot;
    protected boolean hasSurfaceRoot = false;

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

    public int woodRingColor; // For rooty blocks

    public int woodBarkColor; // For rooty water

    private boolean branchIsLadder = true;
    private int maxSignalDepth = 32;

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
     * @param name The Identifier of the tree e.g. "mymod:poplar"
     */
    public Family(Identifier name) {
        this.setRegistryName(name);
        this.commonSpecies = Species.NULL_SPECIES;
    }

    private static DataResult<Family> read(Identifier name) {
        final Family family = Family.REGISTRY.get(name);
        return family == null ? DataResult.error(() -> "Family not found: " + name) : DataResult.success(family);
    }

    @Override
    public final Class<Family> getRegistryType() {
        return REGISTRY.getType();
    }

    protected void addBranch(int index, BranchEntry entry){
        if (index < branches.size()){
            DynamicTrees.LOG.error("Family {} already contains a branch at index {}", this.getRegistryName(), index);
        } else {
            branches.add(index, entry);
        }
    }

    public void setupBlocks() {
        addBranch(BRANCH_INDEX, new BranchEntry(this, getBranchName(""))
                .setCanBeStripped(hasStrippedBranch)
                .CreateBlock(this::createBranch)
                .CreateItem());

        if (hasStrippedBranch()) {
            addBranch(STRIPPED_BRANCH_INDEX, new BranchEntry(this, getBranchName("stripped_"))
                    .CreateBlock(this::createBranch));
        }

        if (this.hasSurfaceRoot()) {
            this.setSurfaceRoot(this.createSurfaceRoot());
        }
    }

    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        return isThick() ? new ThickBranchBlock(name, properties) : new BasicBranchBlock(name, properties);
    }

    protected Identifier getBranchName(final String prefix) {
        return this.getRegistryName().withPrefix(prefix).withSuffix(BranchBlock.NAME_SUFFIX);
    }

    public void setCommonSpecies(final Species species) {
        // Set the common species and auto-generate seeds and saplings unless opted out.
        this.commonSpecies = species
                .setShouldGenerateSeedIfNull(true)
                .setShouldGenerateSaplingIfNull(true)
                .generateSeed()
                .generateSapling();
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
        if (this.hasStrippedBranch() && state.getBlock() instanceof BranchBlock branch) {
            branch.stripBranchAndDamageAxe(state, level, pos, player, heldItem);
            if (level.isClientSide()) {
                level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                WailaHelper.invalidateWailaPosition();
            }
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

    protected Optional<BranchBlock> getBranchBlock(int index) {
        return Optionals.ofBlock(branches.get(index).getBlock());
    }
    protected Optional<Item> getBranchItem(int index) {
        return branches.get(index).getItem();
    }

    public Optional<BranchBlock> getBranch() {
        return getBranchBlock(BRANCH_INDEX);
    }

    public Optional<BranchBlock> getStrippedBranch() {
        return getBranchBlock(STRIPPED_BRANCH_INDEX);
    }

    public Optional<Item> getBranchItem() {
        return getBranchItem(BRANCH_INDEX);
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

    public boolean isThick() {
        return this.maxBranchRadius > BranchBlock.MAX_RADIUS;
    }

    public int getMaxBranchRadius() {
        return this.maxBranchRadius;
    }

    public void setMaxBranchRadius(int maxBranchRadius) {
        this.maxBranchRadius = maxBranchRadius;
    }

//    
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
    public ItemStack getStickStack(int qty) {
        return this.stick == Items.AIR ? ItemStack.EMPTY : new ItemStack(this.stick, Mth.clamp(qty, 0, 64));
    }

    public Item getStick() {
        return stick;
    }

    /**
     * Used to set the type of log item that a tree drops when it's harvested. Use this function to explicitly set the
     * itemstack instead of having it done automatically.
     *
     * @param primitiveLog A block object that is the log
     * @return {@link Family} for chaining calls
     */
    public Family setPrimitiveLog(Block primitiveLog) {
        branches.get(BRANCH_INDEX).setPrimitiveBlock(primitiveLog);
        return this;
    }

    public Family setPrimitiveStrippedLog(Block primitiveStrippedLog) {
        branches.get(STRIPPED_BRANCH_INDEX).setPrimitiveBlock(primitiveStrippedLog);
        return this;
    }

    /**
     * Gets the primitive full block (vanilla)log that represents this tree's material. Chiefly used to determine the
     * wood hardness for harvesting behavior.
     *
     * @return Block of the primitive log.
     */
    public Optional<Block> getPrimitiveLog(int index) {
        return branches.get(index).getPrimitiveBlock();
    }
    public Optional<Block> getPrimitiveLog() {
        return getPrimitiveLog(BRANCH_INDEX);
    }

    public Optional<Block> getPrimitiveStrippedLog() {
        return getPrimitiveLog(STRIPPED_BRANCH_INDEX);
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
    public ToolMaterial getDefaultBranchHarvestTier() {
        return null;
    }

    /**
     * {@code null} = can harvest with hand
     */
    @Nullable
    public ToolMaterial getDefaultStrippedBranchHarvestTier() {
        return null;
    }

    @Deprecated(forRemoval = true)
    public MapColor getDefaultBranchMapColor() {
        return MapColor.WOOD;
    }

    @Deprecated(forRemoval = true)
    public SoundType getDefaultBranchSoundType() {
        return SoundType.WOOD;
    }

    public BlockBehaviour.Properties defaultBranchProperties() {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .sound(SoundType.WOOD)
                .mapColor(MapColor.WOOD)
                .noLootTable()
                .explosionResistance(3.0F);
        if (!this.isFireProof())
            properties.ignitedByLava();
        return properties;
    }

    public void setBranchBlockProperties(BlockBehaviour.Properties properties) {
        this.branchProperties = properties;
    }

    public BlockBehaviour.Properties getBranchProperties() {
        if (branchProperties == null) return defaultBranchProperties();
        return branchProperties;
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

    public int getMinRadiusForStripping() {
        if (minRadiusForStripping == null) return DTConfigs.SERVER.minRadiusForStrip.get();
        return minRadiusForStripping;
    }

    public void setMinRadiusForStripping(int radius) {
        this.minRadiusForStripping = radius;
    }

    public boolean reduceRadiusWhenStripping() {
        if (DTConfigs.SERVER.enableStripRadiusReduction.get())
            return reduceRadiusWhenStripping;
        return false;
    }

    public void setReduceRadiusWhenStripping(boolean reduceRadiusWhenStripping) {
        this.reduceRadiusWhenStripping = reduceRadiusWhenStripping;
    }

    public int getBranchBlockIndex(BranchBlock block) {
        int index = IntStream.range(0, branches.size())
                .filter(i -> branches.get(i).getBlock() == block)
                .findFirst()
                .orElse(-1);
        if (index < 0) {
            DynamicTrees.LOG.warn("Block {} not valid branch for {}.", block, this);
            return 0;
        }
        return index;
    }

    @Nullable
    public BranchBlock getValidBranchBlock(int index) {
        if (index < branches.size())
            return branches.get(index).getBlock();
        else {
            DynamicTrees.LOG.warn("Attempted to get branch block of index {} but {} only has {} valid branches.", index, this, branches.size());
            return this.branches.getFirst().getBlock();
        }
    }

    public int getNumberOfValidBranchBlocks() {
        return branches.size();
    }

    public void setBranchIsLadder(boolean branchIsLadder) {
        this.branchIsLadder = branchIsLadder;
    }

    public boolean branchIsLadder() {
        return branchIsLadder;
    }


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
        Identifier id = suffix(this.getRegistryName(), "_root");
        return RegistryHandler.addBlock(id, () -> new SurfaceRootBlock(id, this, defaultBranchProperties()));
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

    //By default, there is no root species anyway. This is overridden by families like mangrove.
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
    public BlockPosBounds expandLeavesBlockBounds(BlockPosBounds bounds) {
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

    ///////////////////////////////////////////
    // TAG GENERATION
    ///////////////////////////////////////////

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

    public void addGeneratedBlockTags (Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender){
        getBranch().ifPresent(branch -> {
            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
            defaultBranchTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(branch);
                } else {
                    tagAppender.apply(tag).addOptional(branch);
                }
            });
        });

        // Create stripped branch tag and harvest tag if the family has a stripped branch.
        getStrippedBranch().ifPresent(strippedBranch -> {
            tierTag(getDefaultStrippedBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(strippedBranch));
            defaultStrippedBranchTags().forEach(tag ->
            {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(strippedBranch);
                } else {
                    tagAppender.apply(tag).addOptional(strippedBranch);
                }
            });
        });
    }

    protected Optional<TagAppender<Block, Block>> tierTag(@Nullable ToolMaterial tier, Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender) {
        if (tier == null)
            return Optional.empty();

        TagKey<Block> tag = tier.incorrectBlocksForDrops();

        return Optional.of(tagAppender.apply(tag));
    }

    public void addGeneratedItemTags (Function<TagKey<Item>, TagAppender<Item, Item>> tagAppender){
        getBranchItem().ifPresent(item -> {
                    if (!isOnlyIfLoaded()) {
                        defaultBranchItemTags().forEach(tag -> tagAppender.apply(tag).add(item));
                    } else {
                        defaultBranchItemTags().forEach(tag -> tagAppender.apply(tag).addOptional(item));
                    }
                }
        );
    }

    ///////////////////////////////////////////
    // DATA GENERATION
    ///////////////////////////////////////////

    public Identifier getSurfaceRootLoader(){
        return DynamicTrees.location("surface_root");
    }
    public Identifier getBranchLoader(){
        return DynamicTrees.location("branch");
    }

    public Identifier getBranchItemParentLocation() {return DynamicTrees.location("branch");}
    public Identifier getRootItemParentLocation() {return DynamicTrees.location("root_branch");}

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> generators = new LinkedList<>();
        generators.add(DynamicTrees.location("branch"));
        if (hasStrippedBranch())
            generators.add(DynamicTrees.location("stripped_branch"));
        if (hasSurfaceRoot())
            generators.add(DynamicTrees.location("surface_root"));
        return generators;
    }
    @Override
    public List<Identifier> getItemModelGenerators() {
        return List.of(DynamicTrees.location("branch_item"));
    }
    @Override
    public List<Identifier> getLangGenerators() {
        return List.of(DynamicTrees.location("family_lang"));
    }

    protected List<String> onlyIfLoaded = new ArrayList<>();
    //Texture overrides
    protected HashMap<String, Identifier> textureOverrides = new HashMap<>();
    protected HashMap<String, Identifier> modelOverrides = new HashMap<>();
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

    public void setTextureOverrides(Map<String, Identifier> textureOverrides) {
        this.textureOverrides.putAll(textureOverrides);
    }

    public Optional<Identifier> getTexturePath(String key) {
        return Optional.ofNullable(textureOverrides.getOrDefault(key, null));
    }

    public void setModelOverrides(Map<String, Identifier> modelOverrides) {
        this.modelOverrides.putAll(modelOverrides);
    }

    public Optional<Identifier> getModelPath(String key) {
        return Optional.ofNullable(modelOverrides.getOrDefault(key, null));
    }

    public void setLangOverrides(Map<String, String> langOverrides) {
        this.langOverrides.putAll(langOverrides);
    }

    public Optional<String> getLangOverride(String key) {
        return Optional.ofNullable(langOverrides.getOrDefault(key, null));
    }

    public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock) {
        Identifier bark = primitiveLogLocation;
        Identifier rings = suffix(primitiveLogLocation, "_top");

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

    public List<Identifier> topBranchTextureLocations(){
        List<Identifier> locations = new ArrayList<>();
        if (getPrimitiveLog().isPresent()){
            locations.add(topBranchTextureLocation(getPrimitiveLog().get(), BRANCH_TOP));
        }
        if (getPrimitiveStrippedLog().isPresent()){
            locations.add(topBranchTextureLocation(getPrimitiveStrippedLog().get(), STRIPPED_BRANCH_TOP));
        }
        return locations;
    }
    protected Identifier topBranchTextureLocation(Block block, String key){
        if (textureOverrides.containsKey(key)){
            return textureOverrides.get(key);
        } else {
            Identifier textureLoc = BuiltInRegistries.BLOCK.getKey(block);
            textureLoc = surround(textureLoc, "block/", "_top");
            return textureLoc;
        }

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
//                Pair.of("primitiveLog", this.primitiveLog),
//                Pair.of("primitiveStrippedLog", this.primitiveStrippedLog),
                Pair.of("stick", this.stick),
                Pair.of("minRadiusForStrip", this.minRadiusForStripping)
        );
    }
}