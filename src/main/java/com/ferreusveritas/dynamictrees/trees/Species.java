package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.api.data.SaplingStateGenerator;
import com.ferreusveritas.dynamictrees.api.data.SeedItemModelGenerator;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.network.NodeInspector;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.api.substances.Emptiable;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilHelper;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.compat.seasons.NormalSeasonManager;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandler;
import com.ferreusveritas.dynamictrees.event.BiomeSuitabilityEvent;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.FullGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostRotContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PreGenerationContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.DiseaseNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.InflatorNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.systems.nodemappers.ShrinkerNode;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.systems.substances.FertilizeSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.util.BlockStates;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.ferreusveritas.dynamictrees.util.MutableLazyValue;
import com.ferreusveritas.dynamictrees.util.Optionals;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.WorldContext;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeRegistry;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.loot.LootTable.EMPTY;

public class Species extends RegistryEntry<Species> implements Resettable<Species> {

    public static final Species NULL_SPECIES = new Species() {
        @Override
        public Optional<Seed> getSeed() {
            return Optional.empty();
        }

        @Override
        public Family getFamily() {
            return Family.NULL_FAMILY;
        }

        @Override
        public boolean isTransformable() {
            return false;
        }

        @Override
        public boolean plantSapling(IWorld world, BlockPos pos, boolean locationOverride) {
            return false;
        }

        @Override
        public boolean generate(WorldContext worldContext, BlockPos pos,
                                Biome biome, Random random, int radius,
                                SafeChunkBounds safeBounds) {
            return false;
        }

        @Override
        public float biomeSuitability(World world, BlockPos pos) {
            return 0.0f;
        }

        @Override
        public Species setSeed(Seed seed) {
            return this;
        }

        @Override
        public ItemStack getSeedStack(int qty) {
            return ItemStack.EMPTY;
        }

        @Override
        public ITextComponent getTextComponent() {
            return this.formatComponent(new TranslationTextComponent("gui.none"), TextFormatting.DARK_RED);
        }

        @Override
        public boolean update(World world, RootyBlock rootyDirt, BlockPos rootPos, int fertility, TreePart treeBase,
                              BlockPos treePos, Random random, boolean rapid) {
            return false;
        }
    };

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(Species::new);

    public static TypedRegistry.EntryType<Species> createDefaultType(
            final Function3<ResourceLocation, Family, LeavesProperties, Species> constructor) {
        return TypedRegistry.newType(createDefaultCodec(constructor));
    }

    public static Codec<Species> createDefaultCodec(
            final Function3<ResourceLocation, Family, LeavesProperties, Species> constructor) {
        return RecordCodecBuilder.create(instance -> instance
                .group(ResourceLocation.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString())
                                .forGetter(Species::getRegistryName),
                        Family.REGISTRY.getGetterCodec().fieldOf("family").forGetter(Species::getFamily),
                        LeavesProperties.REGISTRY.getGetterCodec().optionalFieldOf("leaves_properties",
                                LeavesProperties.NULL_PROPERTIES).forGetter(Species::getLeavesProperties))
                .apply(instance, constructor));
    }

    /**
     * Central registry for all {@link Species} objects.
     */
    public static final TypedRegistry<Species> REGISTRY = new TypedRegistry<>(Species.class, NULL_SPECIES, TYPE);

    /**
     * The family of tree this belongs to. E.g. "Oak" and "Swamp Oak" belong to the "Oak" Family
     */
    protected Family family = Family.NULL_FAMILY;

    /**
     * Logic kit for standardized extended growth behavior
     */
    protected GrowthLogicKitConfiguration logicKit = GrowthLogicKitConfiguration.getDefault();

    /**
     * How quickly the branch thickens on it's own without branch merges [default = 0.3]
     */
    protected float tapering = 0.3f;
    /**
     * The probability that the direction decider will choose up out of the other possible direction weights [default =
     * 2]
     */
    protected int upProbability = 2;
    /**
     * Number of blocks high we have to be before a branch is allowed to form [default = 3] (Just high enough to walk
     * under)
     */
    protected int lowestBranchHeight = 3;
    /**
     * Ideal signal energy. Greatest possible height that branches can reach from the root node [default = 16]
     */
    protected float signalEnergy = 16.0f;
    /**
     * Ideal growth rate [default = 1.0]
     */
    protected float growthRate = 1.0f;
    /**
     * Ideal soil longevity [default = 8]
     */
    protected int soilLongevity = 8;
    /**
     * The tags for the types of soil the tree can be planted on
     */
    protected int soilTypeFlags = 0;

    // TODO: Make sure this is implemented properly.
    protected int maxBranchRadius = 8;

    /**
     * Stores whether or not this species can be transformed to another, if {@code true} and this species has it's own
     * seed a transformation potion will also be automatically created.
     */
    private boolean transformable = true;

    /**
     * If this is not empty, saplings will only grow when planted on these blocks.
     */
    protected final List<Block> acceptableBlocksForGrowth = Lists.newArrayList();

    //Leaves
    protected LeavesProperties leavesProperties = LeavesProperties.NULL_PROPERTIES;

    /**
     * A list of leaf blocks the species accepts as its own. Used for the falling tree renderer
     */
    private final List<LeavesProperties> validLeaves = new LinkedList<>();

    //Seeds
    /**
     * The seed used to reproduce this species.  Drops from the tree and can plant itself
     */
    protected Seed seed;

    /**
     * A blockState that will turn itself into this tree
     */
    protected DynamicSaplingBlock saplingBlock;

    //WorldGen
    /**
     * A map of environmental biome factors that change a tree's suitability
     */
    protected Map<BiomeDictionary.Type, Float> envFactors = new HashMap<>();//Environmental factors

    protected List<Biome> perfectBiomes = new ArrayList<>();

    protected final List<GenFeatureConfiguration> genFeatures = new ArrayList<>();

    /**
     * A {@link BiPredicate} that returns true if this species should override the common in the given position.
     */
    protected CommonOverride commonOverride;

    private String unlocalizedName = "";

    private Set<Fruit> fruits = new HashSet<>();
    private Set<Pod> pods = new HashSet<>();

    /**
     * Blank constructor for {@link #NULL_SPECIES}.
     */
    public Species() {
        this.setRegistryName(DTTrees.NULL);
    }

    /**
     * Constructor suitable for derivative mods that defaults the leavesProperties to the common type for the family
     *
     * @param name   The simple name of the species e.g. "oak"
     * @param family The {@link Family} that this species belongs to.
     */
    public Species(ResourceLocation name, Family family) {
        this(name, family, family.getCommonLeaves());
    }

    /**
     * Constructor suitable for derivative mods
     *
     * @param name             The simple name of the species e.g. "oak"
     * @param leavesProperties The properties of the leaves to be used for this species
     * @param family           The {@link Family} that this species belongs to.
     */
    public Species(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        this.setRegistryName(name);
        this.setUnlocalizedName(name.toString());
        this.family = family;
        this.family.addSpecies(this);
        this.setLeavesProperties(leavesProperties.isValid() ? leavesProperties : family.getCommonLeaves());
    }

    /**
     * Resets this {@link Species} object's environment factors, gen features, acceptable blocks for growth, and
     * acceptable soils. May also be overridden by sub-classes that need lists to be cleared on reload, for example.
     *
     * @return This {@link Species} object for chaining.
     */
    @Override
    public Species reset() {
        this.fruits.clear();
        this.pods.clear();
        this.envFactors.clear();
        this.genFeatures.clear();
        this.acceptableBlocksForGrowth.clear();
        this.primitiveSaplingRecipe.clear();
        this.perfectBiomes.clear();

        this.clearAcceptableSoils();

        return this;
    }

    /**
     * Can be overridden by sub-classes for setting defaults for things before reload, such as {@link #envFactors}.
     *
     * @return This {@link Species} object for chaining.
     */
    @Override
    public Species setPreReloadDefaults() {
        return this.setDefaultGrowingParameters()
                .setSaplingShape(CommonVoxelShapes.SAPLING)
                .setSaplingSound(SoundType.GRASS)
                .setSeedChances(new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F});
    }

    /**
     * Can be overridden by sub-classes for setting defaults for things after reload. This is for defaults like lists,
     * and so defaults should only be set if there was nothing set by the Json.
     *
     * @return This {@link Species} object for chaining.
     */
    @Override
    public Species setPostReloadDefaults() {
        // If no seed has been set, use the common seed.
        if (!this.hasSeed()) {
            this.seed = this.getCommonSpecies().seed;
        }

        // If there is no acceptable soil set, use the standard soils.
        if (!this.hasAcceptableSoil()) {
            this.setStandardSoils();
        }
        return this;
    }

    /**
     * Can be overridden by sub-classes to set the default growing parameters.
     *
     * @return This {@link Species} object for chaining.
     */
    public Species setDefaultGrowingParameters() {
        return this;
    }

    /**
     * Gets the default chance to use for the {@link #seed} for the {@link net.minecraft.block.ComposterBlock}.
     *
     * @return The default chance for the compostable {@link Seed} to be successfully composted.
     */
    public float defaultSeedComposterChance() {
        return 0.3f;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        family.addSpecies(this);
        this.family = family;
    }

    /**
     * Returns the common {@link Species} of this {@link Species}'s {@link Family}.
     *
     * @return The {@link #family}'s {@link Family#commonSpecies}.
     */
    public Species getCommonSpecies() {
        return this.family.getCommonSpecies();
    }

    /**
     * Checks if this {@link Species} is the common species of its {@link Family} (it equals
     * {@link Family#commonSpecies}).
     *
     * @return {@code true} if this species is the common of {@link #family}; {@code false} otherwise.
     */
    public boolean isCommonSpecies() {
        return this.getCommonSpecies() == this;
    }

    /**
     * Checks whether or not {@link #seed} is the same instance as the {@link Seed} of the common {@link Species} of the
     * owning {@link Family}.
     *
     * @return {@code true} if {@link #seed} {@code ==} the {@link Seed} of the common {@link Species} of
     * {@link #family}; {@code false} otherwise.
     */
    public boolean isSeedCommon() {
        return this.getCommonSpecies().getSeed().orElse(null) == this.seed;
    }

    public Species setUnlocalizedName(String name) {
        this.unlocalizedName = "species." + name.replace(":", ".");
        return this;
    }

    public String getLocalizedName() {
        return I18n.get(this.getUnlocalizedName());
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }

    @Override
    public ITextComponent getTextComponent() {
        return this.formatComponent(new TranslationTextComponent(this.getUnlocalizedName()), TextFormatting.AQUA);
    }

    public Species setBasicGrowingParameters(float tapering, float energy, int upProbability, int lowestBranchHeight,
                                             float growthRate) {
        this.tapering = tapering;
        this.signalEnergy = energy;
        this.upProbability = upProbability;
        this.lowestBranchHeight = lowestBranchHeight;
        this.growthRate = growthRate;
        return this;
    }

    public void setTapering(float tapering) {
        this.tapering = tapering;
    }

    public void setUpProbability(int upProbability) {
        this.upProbability = upProbability;
    }

    public void setLowestBranchHeight(int lowestBranchHeight) {
        this.lowestBranchHeight = lowestBranchHeight;
    }

    public void setSignalEnergy(float signalEnergy) {
        this.signalEnergy = signalEnergy;
    }

    public void setGrowthRate(float growthRate) {
        this.growthRate = growthRate;
    }

    public float getSignalEnergy() {
        return signalEnergy;
    }

    public float getEnergy(World world, BlockPos rootPos) {
        return this.logicKit.getEnergy(new PositionalSpeciesContext(world, rootPos, this));
    }

    public float getGrowthRate(World world, BlockPos rootPos) {
        return this.growthRate * this.seasonalGrowthFactor(WorldContext.create(world), rootPos);
    }

    /**
     * Probability reinforcer for up direction which is arguably the direction most trees generally grow in.
     */
    public int getUpProbability() {
        return upProbability;
    }

    /**
     * Probability reinforcer for current travel direction
     */
    public int getProbabilityForCurrentDir() {
        return 1;
    }

    public int getLowestBranchHeight() {
        return lowestBranchHeight;
    }

    public float getTapering() {
        return tapering;
    }

    /**
     * Works out if this {@link Species} will require a {@link SpeciesTileEntity} at the given position. It should
     * require one if it's not the common species and it's not in its common species override for the given position.
     *
     * @param world The {@link IWorld} the tree is being planted in.
     * @param pos   The {@link BlockPos} at which the tree is being planted at.
     * @return True if it will require a {@link SpeciesTileEntity}.
     */
    public boolean doesRequireTileEntity(IWorld world, BlockPos pos) {
        return !this.isCommonSpecies() && !this.shouldOverrideCommon(world, pos);
    }

    /**
     * Returns whether or not this species can be transformed to another. See {@link #transformable} for more details.
     *
     * @return True if it can be transformed to, false if not.
     */
    public boolean isTransformable() {
        return this.transformable;
    }

    /**
     * Sets whether or not this species can be transformed to another. See {@link #transformable} for more details.
     *
     * @param transformable True if it should be transformable.
     * @return This {@link Species} for chaining.
     */
    public Species setTransformable(boolean transformable) {
        this.transformable = transformable;
        return this;
    }

    public boolean hasCommonOverride() {
        return this.commonOverride != null;
    }

    public void setCommonOverride(final CommonOverride commonOverride) {
        this.commonOverride = commonOverride;
    }

    public boolean shouldOverrideCommon(final IBlockReader world, final BlockPos trunkPos) {
        return this.hasCommonOverride() && this.commonOverride.test(world, trunkPos);
    }

    @FunctionalInterface
    public interface CommonOverride extends BiPredicate<IBlockReader, BlockPos> {

    }

    ///////////////////////////////////////////
    //LEAVES
    ///////////////////////////////////////////

    public Species setLeavesProperties(LeavesProperties leavesProperties) {
        this.leavesProperties = leavesProperties;
        leavesProperties.setFamily(getFamily());
        addValidLeafBlocks(leavesProperties);
        return this;
    }

    public LeavesProperties getLeavesProperties() {
        return leavesProperties;
    }

    public Optional<DynamicLeavesBlock> getLeavesBlock() {
        return this.leavesProperties.getDynamicLeavesBlock();
    }

    public Optional<Block> getPrimitiveLeaves() {
        return Optionals.ofBlock(this.leavesProperties.getPrimitiveLeaves().getBlock());
    }

    public void addValidLeafBlocks(LeavesProperties... leaves) {
        for (LeavesProperties leaf : leaves) {
            if (!this.validLeaves.contains(leaf)) {
                this.validLeaves.add(leaf);
            }
        }
    }

    public int getLeafBlockIndex(DynamicLeavesBlock block) {
        int index = validLeaves.indexOf(block.properties);
        if (index < 0) {
            LogManager.getLogger().warn("Block {} not valid leaves for {}.", block, this);
            return 0;
        }
        return index;
    }

    public LeavesProperties getValidLeavesProperties(int index) {
        if (index < validLeaves.size()) {
            return this.validLeaves.get(index);
        } else {
            LogManager.getLogger()
                    .warn("Attempted to get leaves properties of index {} but {} only has {} valid leaves.", index,
                            this, validLeaves.size());
            return this.validLeaves.get(0);
        }
    }

    public DynamicLeavesBlock getValidLeafBlock(int index) {
        LeavesProperties properties = getValidLeavesProperties(index);
        if (!properties.getDynamicLeavesBlock().isPresent()) {
            return null;
        }
        return (DynamicLeavesBlock) properties.getDynamicLeavesState().getBlock();
    }

    public boolean isValidLeafBlock(final DynamicLeavesBlock leavesBlock) {
        return this.validLeaves.stream().anyMatch(properties ->
                properties.getDynamicLeavesBlock().orElse(null) == leavesBlock);
    }

    public int colorTreeQuads(int defaultColor, FallingTreeEntityModel.TreeQuadData treeQuad) {
        return defaultColor;
    }

    public int leafColorMultiplier(World world, BlockPos pos) {
        return getLeavesProperties().treeFallColorMultiplier(getLeavesProperties().getDynamicLeavesState(), world, pos);
    }

    ///////////////////////////////////////////
    //SEEDS
    ///////////////////////////////////////////

    /**
     * Get an ItemStack of the species {@link Seed} with the supplied quantity.
     *
     * @param qty The number of items in the newly copied stack.
     * @return An {@link ItemStack} with the {@link Seed} inside.
     */
    public ItemStack getSeedStack(int qty) {
        return !this.hasSeed() ? ItemStack.EMPTY : new ItemStack(this.seed, qty);
    }

    public boolean hasSeed() {
        return this.seed != null;
    }

    public Optional<Seed> getSeed() {
        return Optional.ofNullable(this.seed);
    }

    /**
     * Holds whether or not a {@link Seed} should be generated. Stored as a {@code non-primitive} so its default value
     * is {@code null}.
     */
    private Boolean shouldGenerateSeed;

    public boolean shouldGenerateSeed() {
        return this.shouldGenerateSeed != null && this.shouldGenerateSeed;
    }

    public void setShouldGenerateSeed(boolean shouldGenerateSeed) {
        this.shouldGenerateSeed = shouldGenerateSeed;
    }

    /**
     * Sets {@link #shouldGenerateSeed} to the given boolean, only if it's currently {@code null}. This allows for
     * setting a default which can then be overridden by Json.
     *
     * @param shouldGenerateSeed {@code true} if a seed should be generated; {@code false} otherwise.
     * @return This {@link Species} object for chaining.
     */
    public Species setShouldGenerateSeedIfNull(boolean shouldGenerateSeed) {
        if (this.shouldGenerateSeed == null) {
            this.shouldGenerateSeed = shouldGenerateSeed;
        }
        return this;
    }

    private String seedName = null;

    public ResourceLocation getSeedName() {
        if (seedName == null) {
            return ResourceLocationUtils.suffix(getRegistryName(), "_seed");
        } else {
            return new ResourceLocation(getRegistryName().getNamespace(), seedName);
        }
    }

    public void setSeedName(String name) {
        seedName = name;
    }

    /**
     * Generates and registers a {@link Seed} item for this species. Note that it will only be generated if
     * {@link #shouldGenerateSeed} is {@code true}.
     *
     * @return This {@link Species} object for chaining.
     */
    public Species generateSeed() {
        return !this.shouldGenerateSeed() || this.seed != null ? this :
                this.setSeed(RegistryHandler.addItem(getSeedName(), new Seed(this)));
    }

    /**
     * Sets the {@link Seed} object for this {@link Species}.
     *
     * @param seed The {@link Seed} to set.
     * @return This {@link Species} object for chaining.
     */
    public Species setSeed(final Seed seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Chances for leaves to drop seeds. Used in data gen for loot tables.
     */
    private float[] seedChances = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};

    public Species setSeedChances(float[] seedChances) {
        this.seedChances = (float[]) seedChances;
        return this;
    }

    public Species setSeedChances(Collection<Float> seedChances) {
        this.seedChances = new float[seedChances.size()];
        Iterator<Float> iterator = seedChances.iterator();
        for (int i = 0; i < seedChances.size(); i++) {
            this.seedChances[i] = iterator.next();
        }
        return this;
    }

    public List<ItemStack> getLeavesDrops(World world, BlockPos pos, ItemStack tool) {
        if (world.isClientSide) {
            return Collections.emptyList();
        }
        return getLootTable(world.getServer().getLootTables(), species -> species.leavesDropsPath.get())
                .getRandomItems(createLeavesLootContext(world, pos, tool));
    }

    private LootContext createLeavesLootContext(World world, BlockPos pos, ItemStack tool) {
        return new LootContext.Builder(WorldContext.getServerWorldOrThrow(world))
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(pos))
                .withParameter(DTLootParameters.SEASONAL_SEED_DROP_FACTOR, seasonalSeedDropFactor(WorldContext.create(world), pos))
                .withParameter(LootParameters.TOOL, tool)
                .create(DTLootParameterSets.LEAVES);
    }

    public List<ItemStack> getVoluntaryDrops(World world, BlockPos rootPos, int fertility) {
        if (world.isClientSide) {
            return Collections.emptyList();
        }
        return getLootTable(world.getServer().getLootTables(), species -> species.voluntaryDropsPath.get())
                .getRandomItems(createVoluntaryLootContext(world, rootPos, fertility));
    }

    private LootContext createVoluntaryLootContext(World world, BlockPos rootPos, int fertility) {
        return new LootContext.Builder(WorldContext.getServerWorldOrThrow(world))
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(rootPos))
                .withParameter(DTLootParameters.SEASONAL_SEED_DROP_FACTOR,
                        seasonalSeedDropFactor(WorldContext.create(world), rootPos))
                .withParameter(DTLootParameters.FERTILITY, fertility)
                .create(DTLootParameterSets.VOLUNTARY);
    }

    public LootTable getLootTable(LootTableManager lootTables, Function<Species, ResourceLocation> nameFunction) {
        final LootTable table = lootTables.get(nameFunction.apply(this));
        return table == EMPTY ? (this.isCommonSpecies() ? lootTables.get(nameFunction.apply(getCommonSpecies())) : EMPTY) : table;
    }

    public List<ItemStack> getWoodDrops(World world, NetVolumeNode.Volume volume) {
        return getWoodDrops(world, volume, ItemStack.EMPTY);
    }

    public List<ItemStack> getWoodDrops(World world, NetVolumeNode.Volume volume, ItemStack tool) {
        return getWoodDrops(world, volume, tool, null);
    }

    public List<ItemStack> getWoodDrops(World world, NetVolumeNode.Volume volume,
                                        ItemStack tool, @Nullable Float explosionRadius) {
        volume.multiplyVolume(DTConfigs.TREE_HARVEST_MULTIPLIER.get()); // For cheaters.. you know who you are.
        if (world.isClientSide) {
            return Collections.emptyList();
        }
        final List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < family.getNumberOfValidBranchBlocks(); i++) {
            int branchVolume = volume.getRawVolume(i);
            if (branchVolume > 0) {
                final BranchBlock branchBlock = family.getValidBranchBlock(i);
                drops.addAll(getDropsForBranch(world, tool, explosionRadius, branchVolume, branchBlock));
            }
        }
        cleanDropsList(drops);
        return drops;
    }

    private List<ItemStack> getDropsForBranch(World world, ItemStack tool, @Nullable Float explosionRadius,
                                              int branchVolume, BranchBlock branchBlock) {
        return world.getServer().getLootTables().get(branchBlock.getLootTableName())
                .getRandomItems(createWoodLootContext(world, branchVolume, tool, explosionRadius));
    }

    /**
     * Cleans specified drop list by dividing any stacks with a count exceeding the maximum stack size into multiple
     * stacks of the same item.
     */
    private void cleanDropsList(List<ItemStack> drops) {
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop.getItem() == Items.AIR) {
                drops.remove(i--);
            }
            if (drop.getCount() > drop.getMaxStackSize()) {
                final ItemStack copiedStack = drop.copy();
                copiedStack.setCount(drop.getCount() - drop.getMaxStackSize());
                drops.add(copiedStack);
                drop.setCount(drop.getMaxStackSize());
            }
        }
    }

    private LootContext createWoodLootContext(World world, int volume, ItemStack tool,
                                              @Nullable Float explosionRadius) {
        return new LootContext.Builder(WorldContext.getServerWorldOrThrow(world))
                .withParameter(LootParameters.TOOL, tool)
                .withParameter(DTLootParameters.VOLUME, volume)
                .withOptionalParameter(LootParameters.EXPLOSION_RADIUS, explosionRadius)
                .create(DTLootParameterSets.WOOD);
    }

    public static class LogsAndSticks {

        public List<ItemStack> logs;
        public final int sticks;

        public LogsAndSticks(List<ItemStack> logs, int sticks) {
            this.logs = logs;
            this.sticks = DTConfigs.DROP_STICKS.get() ? sticks : 0;
        }

    }

    public LogsAndSticks getLogsAndSticks(NetVolumeNode.Volume volume) {
        List<ItemStack> logsList = new LinkedList<>();
        int[] volArray = volume.getRawVolumesArray();
        float stickVol = 0;
        for (int i = 0; i < volArray.length; i++) {
            float vol = (volArray[i] / (float) NetVolumeNode.Volume.VOXELSPERLOG);
            if (vol > 0) {
                stickVol += getFamily().getValidBranchBlock(i).getPrimitiveLogs(vol, logsList);
            }
        }
        int sticks = (int) (stickVol * 8); // A stick is 1/8th of a log (1 log = 4 planks, 2 planks = 4 sticks) Give him the stick!
        return new LogsAndSticks(logsList, sticks);
    }

    /**
     * @param world
     * @param endPoints
     * @param rootPos
     * @param treePos
     * @param fertility
     * @return true if seed was dropped
     */
    public boolean handleVoluntaryDrops(World world, List<BlockPos> endPoints, BlockPos rootPos, BlockPos treePos,
                                        int fertility) {
        int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if (tickSpeed > 0) {
            double slowFactor = 3.0 / tickSpeed;//This is an attempt to normalize voluntary drop rates.
            if (world.random.nextDouble() < slowFactor) {
                final List<ItemStack> drops = getVoluntaryDrops(world, rootPos, fertility);

                if (!drops.isEmpty() && !endPoints.isEmpty()) {
                    for (ItemStack drop : drops) {
                        BlockPos branchPos = endPoints.get(world.random.nextInt(endPoints.size()));
                        branchPos =
                                branchPos.above();//We'll aim at the block above the end branch. Helps with Acacia leaf block formations
                        BlockPos itemPos =
                                CoordUtils.getRayTraceFruitPos(world, this, treePos, branchPos, SafeChunkBounds.ANY);

                        if (itemPos != BlockPos.ZERO) {
                            ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5, itemPos.getY() + 0.5,
                                    itemPos.getZ() + 0.5, drop);
                            Vector3d motion = new Vector3d(itemPos.getX(), itemPos.getY(), itemPos.getZ()).subtract(
                                    new Vector3d(treePos.getX(), treePos.getY(), treePos.getZ()));
                            float distAngle = 15;//The spread angle(center to edge)
                            float launchSpeed = 4;//Blocks(meters) per second
                            motion = new Vector3d(motion.x, 0, motion.y).normalize()
                                    .yRot((world.random.nextFloat() * distAngle * 2) - distAngle)
                                    .scale(launchSpeed / 20f);
                            itemEntity.setDeltaMovement(motion.x, motion.y, motion.z);
                            return world.addFreshEntity(itemEntity);
                        }
                    }
                }
            }
        }
        return true;
    }


    ///////////////////////////////////////////
    // SAPLING
    ///////////////////////////////////////////

    /**
     * Valid primitive sapling {@link Item}s. Used for dirt bucket recipes.
     */
    protected final Set<SeedSaplingRecipe> primitiveSaplingRecipe = new HashSet<>();

    public void addPrimitiveSaplingRecipe(SeedSaplingRecipe recipe) {
        recipe.getSaplingBlock()
                .ifPresent(block -> TreeRegistry.registerSaplingReplacer(block.defaultBlockState(), this));
        primitiveSaplingRecipe.add(recipe);
    }

    public Set<SeedSaplingRecipe> getPrimitiveSaplingRecipes() {
        return new HashSet<>(this.primitiveSaplingRecipe);
    }

    public Species addPrimitiveSaplingItem(final Item primitiveSaplingItem) {
        this.primitiveSaplingRecipe.add(new SeedSaplingRecipe(primitiveSaplingItem));
        return this;
    }

    public Species setSapling(DynamicSaplingBlock sapling) {
        saplingBlock = sapling;
        return this;
    }

    /**
     * Holds whether or not a {@link Seed} should be generated. Stored as a {@code non-primitive} so its default value
     * is {@code null}.
     */
    private Boolean shouldGenerateSapling;

    public boolean shouldGenerateSapling() {
        return this.shouldGenerateSapling != null && this.shouldGenerateSapling;
    }

    public void setShouldGenerateSapling(boolean shouldGenerateSapling) {
        this.shouldGenerateSapling = shouldGenerateSapling;
    }

    /**
     * Sets {@link #shouldGenerateSapling} to the given boolean, only if it's currently {@code null}. This allows for
     * setting a default which can then be overridden by Json.
     *
     * @param shouldGenerateSapling {@code true} if a sapling should be generated; {@code false} otherwise.
     * @return This {@link Species} object for chaining.
     */
    public Species setShouldGenerateSaplingIfNull(boolean shouldGenerateSapling) {
        if (this.shouldGenerateSapling == null) {
            this.shouldGenerateSapling = shouldGenerateSapling;
        }
        return this;
    }

    /**
     * Generates and registers a {@link DynamicLeavesBlock} for this species. Note that it will only be generated if
     * {@link #shouldGenerateSapling} is {@code true}.
     *
     * @return This {@link Species} object for chaining.
     */
    public Species generateSapling() {
        return !this.shouldGenerateSapling() || this.saplingBlock != null ? this :
                this.setSapling(RegistryHandler.addBlock(this.getSaplingRegName(), new DynamicSaplingBlock(this)));
    }

    public Optional<DynamicSaplingBlock> getSapling() {
        return Optional.ofNullable(saplingBlock);
    }

    /**
     * Returns the {@link Species} override for the specified {@link BlockPos} in the specified {@link World} if
     * {@link #shouldUseLocationOverride()}, or returns {@code this} {@link Species} otherwise.
     *
     * @param world The {@link IWorld} to check for the override in.
     * @param pos   The {@link BlockPos} to check.
     * @return The relevant {@link Species} override or {@code this} {@link Species}.
     */
    public Species selfOrLocationOverride(final IBlockReader world, BlockPos pos) {
        return this.shouldUseLocationOverride() ? this.getFamily().getSpeciesForLocation(world, pos, this)
                : this;
    }

    /**
     * Returns {@code true} if the location override should be used for this {@link Species} if available.
     *
     * @return {@code true} if the location override should be used if available, {@code false} otherwise.
     */
    public boolean shouldUseLocationOverride() {
        return !this.getSapling().isPresent() || this.isCommonSpecies();
    }

    /**
     * Checks surroundings and places a dynamic sapling block.
     *
     * @param world
     * @param pos
     * @return true if the planting was successful
     */
    public boolean plantSapling(IWorld world, BlockPos pos, boolean locationOverride) {
        final DynamicSaplingBlock sapling = this.getSapling().orElse(this.getCommonSpecies().saplingBlock);

        if (sapling == null || !world.getBlockState(pos).getMaterial().isReplaceable() ||
                !DynamicSaplingBlock.canSaplingStay(world, this, pos)) {
            return false;
        }

        world.setBlock(pos, sapling.defaultBlockState(), 3);
        return true;
    }

    public void addAcceptableBlockForGrowth(final Block block) {
        this.acceptableBlocksForGrowth.add(block);
    }

    /**
     * Checks if the sapling can grow at the given position.
     *
     * @param world The {@link World} object.
     * @param pos   The {@link BlockPos} the sapling is on.
     * @return True if it can grow.
     */
    public boolean canSaplingGrow(World world, BlockPos pos) {
        return this.acceptableBlocksForGrowth.isEmpty() || this.acceptableBlocksForGrowth.stream()
                .anyMatch(block -> block == world.getBlockState(pos.below()).getBlock());
    }

    private boolean canSaplingGrowNaturally = true;

    public Species setCanSaplingGrowNaturally(boolean canSaplingGrowNaturally) {
        this.canSaplingGrowNaturally = canSaplingGrowNaturally;
        return this;
    }

    /**
     * Determines whether or not the {@link #saplingBlock} should be able to grow without player intervention
     * (bone-mealing).
     *
     * @param world The {@link World} instance.
     * @param pos   The {@link BlockPos} of the {@link DynamicSaplingBlock}.
     * @return {@code true} if the sapling can and should grow naturally; {@code false} otherwise.
     */
    public boolean canSaplingGrowNaturally(World world, BlockPos pos) {
        return this.canSaplingGrowNaturally && this.canSaplingGrow(world, pos);
    }

    //Returns if sapling should consume bonemeal when used on it.
    //if true is returned canSaplingUseBoneMeal is then run to determine if the sapling grows or not.
    public boolean canSaplingConsumeBoneMeal(World world, BlockPos pos) {
        return canBoneMealTree() && canSaplingGrow(world, pos);
    }

    //Returns whether or not the bonemealing should cause sapling growth.
    public boolean canSaplingGrowAfterBoneMeal(World world, Random rand, BlockPos pos) {
        return canBoneMealTree() && canSaplingGrow(world, pos);
    }

    public int saplingFireSpread() {
        return 0;
    }

    public int saplingFlammability() {
        return 0;
    }

    public boolean transitionToTree(World world, BlockPos pos) {

        //Ensure planting conditions are right
        Family family = getFamily();
        if (world.isEmptyBlock(pos.above()) && isAcceptableSoil(world, pos.below(), world.getBlockState(pos.below()))) {
            // Set to a single branch with 1 radius.
            family.getBranch().ifPresent(branch -> branch.setRadius(world, pos, family.getPrimaryThickness(), null));
            // Place a single leaf block on top.
            world.setBlockAndUpdate(pos.above(), getLeavesProperties().getDynamicLeavesState());
            // Set to fully fertilized rooty dirt underneath.
            placeRootyDirtBlock(world, pos.below(), 15);

            if (doesRequireTileEntity(world, pos)) {
                SpeciesTileEntity speciesTE = DTRegistries.speciesTE.create();
                world.setBlockEntity(pos.below(), speciesTE);
                if (speciesTE != null) {
                    speciesTE.setSpecies(this);
                }
            }

            return true;
        }

        return false;
    }

    private VoxelShape saplingShape = CommonVoxelShapes.SAPLING;

    public VoxelShape getSaplingShape() {
        return this.saplingShape;
    }

    public Species setSaplingShape(VoxelShape saplingShape) {
        this.saplingShape = saplingShape;
        return this;
    }

    private String saplingName = null;

    //This is used to load the sapling model
    public ResourceLocation getSaplingRegName() {
        if (saplingName == null) {
            return ResourceLocationUtils.suffix(this.getRegistryName(), "_sapling");
        } else {
            return new ResourceLocation(getRegistryName().getNamespace(), saplingName);
        }
    }

    public void setSaplingName(String name) {
        saplingName = name;
    }

    public int saplingColorMultiplier(BlockState state, IBlockDisplayReader access, BlockPos pos, int tintIndex) {
        return getLeavesProperties().foliageColorMultiplier(state, access, pos);
    }

    private SoundType saplingSound = SoundType.GRASS;

    public SoundType getSaplingSound() {
        return this.saplingSound;
    }

    public Species setSaplingSound(SoundType saplingSound) {
        this.saplingSound = saplingSound;
        return this;
    }

    ///////////////////////////////////////////
    //DIRT
    ///////////////////////////////////////////

    public boolean placeRootyDirtBlock(IWorld world, BlockPos rootPos, int fertility) {
        BlockState dirtState = world.getBlockState(rootPos);
        Block dirt = dirtState.getBlock();

        if (!SoilHelper.isSoilRegistered(dirt) && !(dirt instanceof RootyBlock)) {
            //soil is not valid so we default to dirt
            LogManager.getLogger().warn("Rooty Dirt block NOT FOUND for soil " +
                    dirt.getRegistryName()); //default to dirt and print error
            this.placeRootyDirtBlock(world, rootPos, Blocks.DIRT.defaultBlockState(), fertility);
            return false;
        }

        if (dirt instanceof RootyBlock) {
            //dirt block is already a soil, so we just update it
            this.updateRootyDirtBlock(world, rootPos, dirtState, fertility);
        } else if (SoilHelper.isSoilRegistered(dirt)) {
            this.placeRootyDirtBlock(world, rootPos, dirtState, fertility);
        }

        TileEntity tileEntity = world.getBlockEntity(rootPos);
        if (tileEntity instanceof SpeciesTileEntity) {
            SpeciesTileEntity speciesTE = (SpeciesTileEntity) tileEntity;
            speciesTE.setSpecies(this);
        }

        return true;
    }

    private void placeRootyDirtBlock(IWorld world, BlockPos rootPos, BlockState primitiveDirtState, int fertility) {
        final SoilProperties soilProperties = SoilHelper.getProperties(primitiveDirtState.getBlock());
        soilProperties.getBlock().ifPresent(block ->
                world.setBlock(rootPos, soilProperties.getSoilState(primitiveDirtState, fertility,
                        this.doesRequireTileEntity(world, rootPos)), 3)
        );
    }

    private void updateRootyDirtBlock(IWorld world, BlockPos rootPos, BlockState soilState, int fertility) {
        if (soilState.getBlock() instanceof RootyBlock) {
            world.setBlock(rootPos, soilState.setValue(RootyBlock.FERTILITY, fertility)
                    .setValue(RootyBlock.IS_VARIANT, this.doesRequireTileEntity(world, rootPos)), 3);
        }
    }

    public Species setSoilLongevity(int longevity) {
        soilLongevity = longevity;
        return this;
    }

    public int getSoilLongevity(World world, BlockPos rootPos) {
        return (int) (biomeSuitability(world, rootPos) * soilLongevity);
    }

    public boolean isThick() {
        return this.maxBranchRadius > BranchBlock.MAX_RADIUS;
    }

    public int getMaxBranchRadius() {
        return this.maxBranchRadius;
    }

    public void setMaxBranchRadius(int maxBranchRadius) {
        this.maxBranchRadius = MathHelper.clamp(maxBranchRadius, 1, this.getFamily().getMaxBranchRadius());
    }

    public Species addAcceptableSoils(String... soilTypes) {
        soilTypeFlags |= SoilHelper.getSoilFlags(soilTypes);
        return this;
    }

    /**
     * Will clear the acceptable soils list.  Useful for making trees that can only be planted in abnormal substrates.
     */
    public Species clearAcceptableSoils() {
        soilTypeFlags = 0;
        return this;
    }

    /**
     * This is run by the Species class itself to set the standard blocks available to be used as planting substrate.
     * Developer may override this entirely or just modify the list at a later time.
     */
    protected void setStandardSoils() {
        addAcceptableSoils(SoilHelper.DIRT_LIKE);
    }

    public boolean hasAcceptableSoil() {
        return this.soilTypeFlags != 0;
    }

    /**
     * Soil acceptability tester.  Mostly to test if the block is dirt but could be overridden to allow gravel, sand, or
     * whatever makes sense for the tree species.
     *
     * @param soilBlockState
     * @return
     */
    public boolean isAcceptableSoil(BlockState soilBlockState) {
        return SoilHelper.isSoilAcceptable(soilBlockState, soilTypeFlags);
    }

    /**
     * Position sensitive soil acceptability tester.  Mostly to test if the block is dirt but could be overridden to
     * allow gravel, sand, or whatever makes sense for the tree species.
     *
     * @param world
     * @param pos
     * @param soilBlockState
     * @return
     */
    public boolean isAcceptableSoil(IWorldReader world, BlockPos pos, BlockState soilBlockState) {
        return isAcceptableSoil(soilBlockState);
    }

    /**
     * Version of soil acceptability tester that is only run for worldgen.  This allows for Swamp oaks and stuff.
     *
     * @param world
     * @param pos
     * @param soilBlockState
     * @return
     */
    public boolean isAcceptableSoilForWorldgen(IWorld world, BlockPos pos, BlockState soilBlockState) {
        final boolean isAcceptableSoil = isAcceptableSoil(world, pos, soilBlockState);

        // If the block is water, check the block below it is valid soil (and not water).
        if (isAcceptableSoil && isWater(soilBlockState)) {
            final BlockPos down = pos.below();
            final BlockState downState = world.getBlockState(pos.below());

            return !isWater(downState) && this.isAcceptableSoil(world, down, downState);
        }

        return isAcceptableSoil;
    }

    protected boolean isWater(BlockState soilBlockState) {
        return SoilHelper.isSoilAcceptable(soilBlockState, SoilHelper.getSoilFlags(SoilHelper.WATER_LIKE));
    }


    //////////////////////////////
    // GROWTH
    //////////////////////////////

    /**
     * Basic update. This handles everything for the species Rot, Drops, Fruit, Disease, and Growth respectively. If the
     * rapid option is enabled then drops, fruit and disease are skipped.
     * <p>
     * This should never be run by the world generator.
     *
     * @param world     The world
     * @param rootyDirt The {@link RootyBlock} that is supporting this tree
     * @param rootPos   The {@link BlockPos} of the {@link RootyBlock} type in the world
     * @param fertility The fertility of the soil. 0: Depleted -> 15: Full
     * @param treePos   The {@link BlockPos} of the {@link Family} trunk base.
     * @param random    A random number generator
     * @param natural   Set this to true if this member is being used to naturally grow the tree(create drops or fruit)
     * @return true if network is viable.  false if network is not viable(will destroy the {@link RootyBlock} this tree
     * is on)
     */
    public boolean update(World world, RootyBlock rootyDirt, BlockPos rootPos, int fertility, TreePart treeBase,
                          BlockPos treePos, Random random, boolean natural) {

        //Analyze structure to gather all of the endpoints.  They will be useful for this entire update
        List<BlockPos> ends = getEnds(world, treePos, treeBase);

        //This will prune rotted positions from the world and the end point list
        if (handleRot(world, ends, rootPos, treePos, fertility, SafeChunkBounds.ANY)) {
            return false;//Last piece of tree rotted away.
        }

        if (natural) {
            //This will handle seed drops
            handleVoluntaryDrops(world, ends, rootPos, treePos, fertility);

            //This will handle disease chance
            if (handleDisease(world, treeBase, treePos, random, fertility)) {
                return true;//Although the tree may be diseased. The tree network is still viable.
            }
        }

        return grow(world, rootyDirt, rootPos, fertility, treeBase, treePos, random, natural);
    }

    /**
     * A little internal convenience function for getting branch endpoints
     *
     * @param world    The world
     * @param treePos  The {@link BlockPos} of the base of the {@link Family} trunk
     * @param treeBase The tree part that is the base of the {@link Family} trunk.  Provided for easy analysis.
     * @return A list of all branch endpoints for the {@link Family}
     */
    final protected List<BlockPos> getEnds(World world, BlockPos treePos, TreePart treeBase) {
        FindEndsNode endFinder = new FindEndsNode();
        treeBase.analyse(world.getBlockState(treePos), world, treePos, null, new MapSignal(endFinder));
        return endFinder.getEnds();
    }

    /**
     * A postRot handler.
     *
     * @param world      The world
     * @param ends       A {@link List} of {@link BlockPos}s of {@link BranchBlock} endpoints.
     * @param rootPos    The {@link BlockPos} of the {@link RootyBlock} for this {@link Family}
     * @param treePos    The {@link BlockPos} of the trunk base for this {@link Family}
     * @param fertility  The fertility of the {@link RootyBlock}
     * @param safeBounds The defined boundaries where it is safe to make block changes
     * @return true if last piece of tree rotted away.
     */
    public boolean handleRot(IWorld world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int fertility,
                             SafeChunkBounds safeBounds) {

        Iterator<BlockPos> iter = ends.iterator();//We need an iterator since we may be removing elements.
        SimpleVoxmap leafMap = getLeavesProperties().getCellKit().getLeafCluster();

        while (iter.hasNext()) {
            BlockPos endPos = iter.next();
            BlockState branchState = world.getBlockState(endPos);
            BranchBlock branch = TreeHelper.getBranch(branchState);
            if (branch != null) {
                int radius = branch.getRadius(branchState);
                float rotChance = rotChance(world, endPos, world.getRandom(), radius);
                if (branch.checkForRot(world, endPos, this, fertility, radius, world.getRandom(), rotChance,
                        safeBounds != SafeChunkBounds.ANY) || radius != family.getPrimaryThickness()) {
                    if (safeBounds != SafeChunkBounds.ANY) { // worldgen
                        TreeHelper.ageVolume(world, endPos.below((leafMap.getLenZ() - 1) / 2),
                                (leafMap.getLenX() - 1) / 2, leafMap.getLenY(), 2, safeBounds);
                    }
                    iter.remove(); // Prune out the rotted end points so we don't spawn fruit from them.
                }
            }
        }

        return ends.isEmpty() &&
                !TreeHelper.isBranch(world.getBlockState(treePos));//There are no endpoints and the trunk is missing
    }

    static private final Direction[] upFirst =
            {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private boolean doesRot = true;

    public void setDoesRot(boolean doesRot) {
        this.doesRot = doesRot;
    }

    /**
     * Handles rotting branches.
     *
     * @param world         The world
     * @param pos           The {@link BlockPos}.
     * @param neighborCount Count of neighbors reinforcing this block
     * @param radius        The radius of the branch
     * @param fertility     The fertility of the tree.
     * @param random        Access to a random number generator
     * @param rapid         True if this rot is happening under a generation scenario as opposed to natural tree
     *                      updates
     * @param growLeaves    {@code true} if this rot should attempt to grow leaves first.
     * @return true if the branch should rot
     */
    public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, int fertility, Random random,
                       boolean rapid, boolean growLeaves) {
        if (!doesRot) {
            return false;
        }
        if (radius <= family.getPrimaryThickness()) {
            if (!getLeavesProperties().getDynamicLeavesBlock().isPresent()) {
                return false;
            }

            if (growLeaves) {
                final DynamicLeavesBlock leaves =
                        (DynamicLeavesBlock) getLeavesProperties().getDynamicLeavesState().getBlock();

                for (Direction dir : upFirst) {
                    if (leaves.growLeavesIfLocationIsSuitable(world, getLeavesProperties(), pos.relative(dir), 0)) {
                        return false;
                    }
                }
            }
        }


        if (rapid || (DTConfigs.MAX_BRANCH_ROT_RADIUS.get() != 0 && radius <= DTConfigs.MAX_BRANCH_ROT_RADIUS.get())) {
            BranchBlock branch = TreeHelper.getBranch(world.getBlockState(pos));
            if (branch != null) {
                branch.rot(world, pos);
            }
            this.postRot(new PostRotContext(world, pos, this, radius, neighborCount, fertility, rapid));
            return true;
        }

        return false;
    }

    /**
     * @deprecated No longe in use due to extra parameter. Use/override
     * {@link #rot(IWorld, BlockPos, int, int, int, Random, boolean, boolean)} instead.
     */
    @Deprecated
    public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
        return false;
    }

    public void postRot(PostRotContext context) {
        this.genFeatures.forEach(configuration -> configuration.generate(GenFeature.Type.POST_ROT, context));
    }

    /**
     * Provides the chance that a log will postRot.
     *
     * @param world  The world
     * @param pos    The {@link BlockPos} of the {@link BranchBlock}
     * @param rand   A random number generator
     * @param radius The radius of the {@link BranchBlock}
     * @return The chance this will postRot. 0.0(never) -> 1.0(always)
     */
    public float rotChance(IWorld world, BlockPos pos, Random rand, int radius) {
        if (radius == 0) {
            return 0;
        }
        return 0.3f + ((1f / radius));// Thicker branches take longer to postRot
    }

    /**
     * The grow handler.
     *
     * @param world     The world
     * @param rootyDirt The {@link RootyBlock} that is supporting this tree
     * @param rootPos   The {@link BlockPos} of the {@link RootyBlock} type in the world
     * @param fertility The fertility of the soil. 0: Depleted -> 15: Full
     * @param treePos   The {@link BlockPos} of the {@link Family} trunk base.
     * @param random    A random number generator
     * @param natural   If true then this member is being used to grow the tree naturally(create drops or fruit). If
     *                  false then this member is being used to grow a tree with a growth accelerant like bonemeal or
     *                  the potion of burgeoning
     * @return true if network is viable.  false if network is not viable(will destroy the {@link RootyBlock} this tree
     * is on)
     */
    public boolean grow(World world, RootyBlock rootyDirt, BlockPos rootPos, int fertility, TreePart treeBase,
                        BlockPos treePos, Random random, boolean natural) {

        float growthRate = (float) (getGrowthRate(world, rootPos) * DTConfigs.TREE_GROWTH_MULTIPLIER.get() *
                DTConfigs.TREE_GROWTH_FOLDING.get());
        do {
            if (fertility > 0) {
                if (growthRate > random.nextFloat()) {
                    final GrowSignal signal = new GrowSignal(this, rootPos, getEnergy(world, rootPos), world.random);
                    boolean success = treeBase.growSignal(world, treePos, signal).success;

                    int soilLongevity = getSoilLongevity(world, rootPos) *
                            (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed

                    if (soilLongevity <= 0 || random.nextInt(soilLongevity) ==
                            0) {//1 in X(soilLongevity) chance to draw nutrients from soil
                        rootyDirt.setFertility(world, rootPos, fertility - 1);//decrement fertility
                    }

                    if (signal.choked) {
                        fertility = 0;
                        rootyDirt.setFertility(world, rootPos, fertility);
                        TreeHelper.startAnalysisFromRoot(world, rootPos,
                                new MapSignal(new ShrinkerNode(signal.getSpecies())));
                    }
                }
            }
        } while (--growthRate > 0.0f);

        this.postGrow(world, rootPos, treePos, fertility, natural);
        return true;
    }

    public Species setGrowthLogicKit(GrowthLogicKit logicKit) {
        this.logicKit = logicKit.getDefaultConfiguration();
        return this;
    }

    /**
     * Set the logic kit used to determine how the tree branch network expands. Provides an alternate and more modular
     * method to override a trees growth logic.
     *
     * @param logicKit A growth logic kit
     * @return this species for chaining
     */
    public Species setGrowthLogicKit(GrowthLogicKitConfiguration logicKit) {
        this.logicKit = logicKit;
        return this;
    }

    public GrowthLogicKitConfiguration getGrowthLogicKit() {
        return logicKit;
    }

    private boolean canBoneMealTree = true;

    public void setCanBoneMealTree(boolean canBoneMealTree) {
        this.canBoneMealTree = canBoneMealTree;
    }

    public boolean canBoneMealTree() {
        return canBoneMealTree;
    }

    /**
     * Allows a species to do things after a grow event just occurred. Such as used by Jungle trees to create cocoa pods
     * on the trunk.
     *
     * @param world     The world
     * @param rootPos   The position of the rooty dirt block
     * @param treePos   The position of the base trunk block of the tree(usually directly above the rooty dirt block)
     * @param fertility The fertility of the soil block this tree is planted in
     * @param natural   If true then this member is being used to grow the tree naturally (create drops or fruit). If
     *                  false then this member is being used to grow a tree with a growth accelerant like bonemeal or
     *                  the potion of burgeoning.
     */
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int fertility, boolean natural) {
        this.genFeatures.forEach(configuration ->
                configuration.generate(
                        GenFeature.Type.POST_GROW,
                        new PostGrowContext(world, rootPos, this, treePos, fertility, natural)
                ));
        return true;
    }

    /**
     * Decide what happens for diseases.
     *
     * @param world
     * @param baseTreePart
     * @param treePos
     * @param random
     * @return true if the tree became diseased
     */
    public boolean handleDisease(World world, TreePart baseTreePart, BlockPos treePos, Random random, int fertility) {
        if (fertility == 0 && DTConfigs.DISEASE_CHANCE.get() > random.nextFloat()) {
            baseTreePart.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN,
                    new MapSignal(new DiseaseNode(this)));
            return true;
        }

        return false;
    }


    //////////////////////////////
    // BIOME HANDLING
    //////////////////////////////

    public Species envFactor(BiomeDictionary.Type type, float factor) {
        envFactors.put(type, factor);
        return this;
    }

    /**
     * @param world The {@link World} object.
     * @param pos
     * @return range from 0.0 - 1.0.  (0.0f for completely unsuited.. 1.0f for perfectly suited)
     */
    public float biomeSuitability(World world, BlockPos pos) {

        Biome biome = world.getBiome(pos);

        //An override to allow other mods to change the behavior of the suitability for a world location. Such as Terrafirmacraft.
        BiomeSuitabilityEvent suitabilityEvent = new BiomeSuitabilityEvent(world, biome, this, pos);
        MinecraftForge.EVENT_BUS.post(suitabilityEvent);
        if (suitabilityEvent.isHandled()) {
            return suitabilityEvent.getSuitability();
        }

        float ugs = (float) (double) DTConfigs.SCALE_BIOME_GROWTH_RATE.get(); // Universal growth scalar.

        if (ugs == 1.0f || this.isBiomePerfect(biome)) {
            return 1.0f;
        }

        float suit = defaultSuitability();

        for (BiomeDictionary.Type t : BiomeDictionary.getTypes(
                RegistryKey.create(net.minecraft.util.registry.Registry.BIOME_REGISTRY, biome.getRegistryName()))) {
            suit *= envFactors.getOrDefault(t, 1.0f);
        }

        //Linear interpolation of suitability with universal growth scalar
        suit = ugs <= 0.5f ? ugs * 2.0f * suit : ((1.0f - ugs) * suit + (ugs - 0.5f)) * 2.0f;

        return MathHelper.clamp(suit, 0.0f, 1.0f);
    }

    /**
     * Used to determine if the provided {@link Biome} argument will yield unhindered growth to Maximum potential. This
     * has the affect of the suitability being 100%(or 1.0f)
     *
     * @param biome The biome being tested
     * @return True if biome is "perfect" false otherwise.
     */
    public boolean isBiomePerfect(final Biome biome) {
        return this.perfectBiomes.contains(biome);
    }

    /**
     * Used to determine if the provided {@link Biome} argument will yield unhindered growth to Maximum potential. This
     * has the affect of the suitability being 100%(or 1.0f)
     *
     * @param biome The biome being tested
     * @return True if biome is "perfect" false otherwise.
     */
    public boolean isBiomePerfect(RegistryKey<Biome> biome) {
        return false;
    }

    public List<Biome> getPerfectBiomes() {
        return perfectBiomes;
    }

    public static Biome getBiome(final RegistryKey<Biome> biomeKey) {
        return Objects.requireNonNull(ForgeRegistries.BIOMES.getValue(biomeKey.getRegistryName()));
    }

    public static RegistryKey<Biome> getBiomeKey(final Biome biome) {
        return RegistryKey.create(net.minecraft.util.registry.Registry.BIOME_REGISTRY,
                Objects.requireNonNull(biome.getRegistryName()));
    }

    /**
     * A value that determines what a tree's suitability is before climate manipulation occurs.
     */
    public static float defaultSuitability() {
        return 0.85f;
    }

    /**
     * A convenience function to test if a biome is one of the many options passed.
     *
     * @param biomeToCheck The biome we are matching
     * @param biomes       Multiple biomes to match against
     * @return True if a match is found. False if not.
     */
    @SafeVarargs
    public static boolean isOneOfBiomes(RegistryKey<Biome> biomeToCheck, RegistryKey<Biome>... biomes) {
        for (RegistryKey<Biome> biome : biomes) {
            if (biomeToCheck.equals(biome)) {
                return true;
            }
        }
        return false;
    }


    //////////////////////////////
    // SEASONAL
    //////////////////////////////

    /**
     * default flower holding is relative to the flowering offset, but default is first half of spring
     */
    protected float flowerSeasonHoldMin = SeasonHelper.SPRING;
    protected float flowerSeasonHoldMax = SeasonHelper.SPRING + 0.5f;

    @Nullable
    protected Float seasonalGrowthOffset = 0f;
    @Nullable
    protected Float seasonalSeedDropOffset = 0f;
    @Nullable
    protected Float seasonalFruitingOffset = 0f;

    public void setSeasonalGrowthOffset(@Nullable Float offset) {
        seasonalGrowthOffset = offset;
    }

    public void setSeasonalSeedDropOffset(@Nullable Float offset) {
        seasonalSeedDropOffset = offset;
    }

    /**
     * The default fruiting will PEAK in the middle of summer, starting at the middle of spring and ending at the middle
     * of fall. this offset will move the fruiting by a factor of one season. (an offset of 2.0 will ma fruiting peak in
     * winter). set to null for it to be all year round
     */
    public void setSeasonalFruitingOffset(@Nullable Float offset) {
        seasonalFruitingOffset = offset;
    }

    /**
     * Pulls data from the {@link NormalSeasonManager} to determine the rate of tree growth for the current season.
     *
     * @param rootPos the {@link BlockPos} of the {@link RootyBlock}.
     * @return Factor from 0.0 (no growth) to 1.0 (full growth).
     */
    public float seasonalGrowthFactor(WorldContext worldContext, BlockPos rootPos) {
        return seasonalGrowthOffset != null ?
                SeasonHelper.globalSeasonalGrowthFactor(worldContext, rootPos, -seasonalGrowthOffset) : 1.0f;
    }

    public float seasonalSeedDropFactor(WorldContext worldContext, BlockPos pos) {
        return seasonalSeedDropOffset != null ?
                SeasonHelper.globalSeasonalSeedDropFactor(worldContext, pos, -seasonalSeedDropOffset) : 1.0f;
    }

    public float seasonalFruitProductionFactor(WorldContext worldContext, BlockPos pos) {
        return seasonalFruitingOffset != null ?
                SeasonHelper.globalSeasonalFruitProductionFactor(worldContext, pos, -seasonalFruitingOffset, false)
                : 1.0F;
    }

    // TODO: Update for data-driven fruit

    /**
     * 1 = Spring 2 = Summer 4 = Autumn 8 = Winter Values are OR'ed together for the return
     */
    public int getSeasonalTooltipFlags(final World world) {
        final float seasonStart = 1f / 6;
        final float seasonEnd = 1 - 1f / 6;
        final float threshold = 0.75f;

        if (this.hasFruits()) {
            int seasonFlags = 0;
            for (int i = 0; i < 4; i++) {
                boolean isValidSeason = false;
                if (seasonalFruitingOffset != null) {
                    final WorldContext worldContext = WorldContext.create(world);
                    final float prod1 = SeasonHelper.globalSeasonalFruitProductionFactor(worldContext,
                            new BlockPos(0, (int) ((i + seasonStart - seasonalFruitingOffset) * 64.0f), 0), true);
                    final float prod2 = SeasonHelper.globalSeasonalFruitProductionFactor(worldContext,
                            new BlockPos(0, (int) ((i + seasonEnd - seasonalFruitingOffset) * 64.0f), 0), true);
                    if (Math.min(prod1, prod2) > threshold) {
                        isValidSeason = true;
                    }

                } else {
                    isValidSeason = true;
                }

                if (isValidSeason) {
                    seasonFlags |= 1 << i;
                }

            }
            return seasonFlags;
        }

        return 0;
    }

    /**
     * When seasons are active allow a seasonal time range where fruit growth does not progress past the flower stage.
     * This allows for a flowery spring time.
     *
     * @param min The minimum season value relative to the fruiting offset.
     * @param max The maximum season value relative to the fruiting offset.
     * @return This {@link Species} object for chaining.
     */
    public Species setFlowerSeasonHold(float min, float max) {
        flowerSeasonHoldMin = min;
        flowerSeasonHoldMax = max;
        return this;
    }

    public boolean testFlowerSeasonHold(Float seasonValue) {
        if (seasonalFruitingOffset == null) {
            return false;
        }
        return SeasonHelper.isSeasonBetween(seasonValue, flowerSeasonHoldMin + seasonalFruitingOffset,
                flowerSeasonHoldMax + seasonalFruitingOffset);
    }


    //////////////////////////////
    // INTERACTIVE
    //////////////////////////////

    @Nullable
    public SubstanceEffect getSubstanceEffect(ItemStack itemStack) {

        // Bonemeal fertilizes the soil and causes a single growth pulse.
        if (canBoneMealTree() && itemStack.getItem().is(DTItemTags.FERTILIZER)) {
            return new FertilizeSubstance().setAmount(2).setGrow(true)
                    .setPulses(DTConfigs.BONE_MEAL_GROWTH_PULSES::get);
        }

        // Use substance provider interface if it's available.
        if (itemStack.getItem() instanceof SubstanceEffectProvider) {
            SubstanceEffectProvider provider = (SubstanceEffectProvider) itemStack.getItem();
            return provider.getSubstanceEffect(itemStack);
        }

        // Enhanced fertilizer applies the Burgeoning potion effect.
        if (itemStack.getItem().is(DTItemTags.ENHANCED_FERTILIZER)) {
            return new GrowthSubstance();
        }

        return null;
    }

    /**
     * Apply an item to the treepart(e.g. bonemeal). Developer is responsible for decrementing itemStack after
     * applying.
     *
     * @param world     The current world
     * @param hitPos    Position
     * @param player    The player applying the substance
     * @param itemStack The itemstack to be used.
     * @return true if item was used, false otherwise
     */
    public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, PlayerEntity player, Hand hand,
                                  ItemStack itemStack) {
        final SubstanceEffect effect = getSubstanceEffect(itemStack);

        if (effect != null) {
            boolean applied = effect.apply(world, rootPos);
            if (applied && effect.isLingering()) {
                world.addFreshEntity(new LingeringEffectorEntity(world, rootPos, effect));
                return true;
            } else {
                return applied;
            }
        }

        return false;
    }

    /**
     * Called when a player right clicks a {@link Species} of tree anywhere on it's branches.
     *
     * @return True if action was handled, false otherwise.
     */
    public boolean onTreeActivated(Family.TreeActivationContext context) {
        if (context.heldItem != null) { // Ensure there is something in the player's hand.
            if (applySubstance(context.world, context.rootPos, context.hitPos, context.player, context.hand,
                    context.heldItem)) {
                consumePlayerItem(context.player, context.hand, context.heldItem);
                return true;
            }
        }

        return false;
    }

    /**
     * A convenience function to decrement or otherwise consume an item in use.
     *
     * @param player   The player
     * @param hand     Hand holding the item
     * @param heldItem The item to be consumed
     */
    public static void consumePlayerItem(PlayerEntity player, Hand hand, ItemStack heldItem) {
        if (!player.isCreative()) {
            if (heldItem.getItem() instanceof Emptiable) { // A substance deployed from a refillable container.
                final Emptiable emptiable = (Emptiable) heldItem.getItem();
                player.setItemInHand(hand, emptiable.getEmptyContainer());
            } else if (heldItem.getItem() == Items.POTION) { // An actual potion.
                player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            } else {
                heldItem.shrink(1); // Just a regular item like bonemeal.
            }
        }
    }

    /**
     * The Waila body is the part of the Waila display that shows the species and log/stick count This does not have a
     * Tree Pack implementation as coding is required for it to be useful
     *
     * @return true if the tree uses the default Waila body display. False if it has a custom one (disabling DT's
     * display)
     */
    public boolean useDefaultWailaBody() {
        return true;
    }

    /**
     * If left null, the showSpeciesOnWaila will depend on the species being the common species Otherwise, setting to
     * true or false will force the waila to display or to not display.
     */
    protected Boolean alwaysShowOnWaila = null;

    public Species setAlwaysShowOnWaila(final boolean alwaysShowOnWaila) {
        this.alwaysShowOnWaila = alwaysShowOnWaila;
        return this;
    }

    public boolean showSpeciesOnWaila() {
        if (alwaysShowOnWaila == null) {
            return this != getFamily().getCommonSpecies();
        }
        return this.alwaysShowOnWaila;
    }

    ///////////////////////////////////////////
    // MEGANESS
    ///////////////////////////////////////////

    private Species megaSpecies = Species.NULL_SPECIES;
    private boolean isMegaSpecies = false;

    public Species getMegaSpecies() {
        return this.megaSpecies;
    }

    public boolean isMegaSpecies() {
        return isMegaSpecies;
    }

    public void setMegaSpecies(final Species megaSpecies) {
        this.megaSpecies = megaSpecies;
        megaSpecies.isMegaSpecies = true;
    }

    ///////////////////////////////////////////
    // FALL ANIMATION HANDLING
    ///////////////////////////////////////////

    public AnimationHandler selectAnimationHandler(FallingTreeEntity fallingEntity) {
        return getFamily().selectAnimationHandler(fallingEntity);
    }

    /**
     * This is used for trees that have leaves that are not cubes and require extra blockstate properties such as palm
     * fronds. Used for tree felling animation.
     *
     * @return
     */
    @Nullable
    public HashMap<BlockPos, BlockState> getFellingLeavesClusters(final BranchDestructionData destructionData) {
        return null;
    }

    //////////////////////////////
    // BONSAI POT
    //////////////////////////////

    /**
     * Provides the {@link PottedSaplingBlock} for this Species. {@link Species} subclasses can derive their own
     * {@link PottedSaplingBlock} subclass if they want something custom.
     *
     * @return The {@link PottedSaplingBlock} for this {@link Species}.
     */
    public PottedSaplingBlock getPottedSapling() {
        return DTRegistries.POTTED_SAPLING;
    }


    //////////////////////////////
    // WORLDGEN
    //////////////////////////////

    /**
     * Default worldgen spawn mechanism. This method uses JoCodes to generate tree models. Override to use other
     * methods.
     *
     * @param rootPos The position of {@link RootyBlock} this tree is planted in
     * @param biome   The biome this tree is generating in
     * @param radius  The radius of the tree generation boundary
     * @return true if tree was generated. false otherwise.
     */
    public boolean generate(WorldContext worldContext, BlockPos rootPos, Biome biome, Random random, int radius,
                            SafeChunkBounds safeBounds) {
        final AtomicBoolean fullGen = new AtomicBoolean(false);
        final FullGenerationContext context =
                new FullGenerationContext(worldContext.access(), rootPos, this, biome, radius, safeBounds);

        this.genFeatures.forEach(configuration ->
                fullGen.set(fullGen.get() || configuration.generate(GenFeature.Type.FULL, context))
        );

        if (fullGen.get()) {
            return true;
        }

        final Direction facing = CoordUtils.getRandomDir(random);
        if (!JoCodeRegistry.getCodes(this.getRegistryName()).isEmpty()) {
            final JoCode code = JoCodeRegistry.getRandomCode(this.getRegistryName(), radius, random);
            if (code != null) {
                code.generate(worldContext, this, rootPos, biome, facing, radius, safeBounds, false);
                return true;
            }
        }

        return false;
    }

    public JoCode getJoCode(String joCodeString) {
        return new JoCode(joCodeString);
    }

    public Collection<JoCode> getJoCodes() {
        return JoCodeRegistry.getCodes(this.getRegistryName()).values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Adds the default configuration of the {@link GenFeature} given.
     * <p>
     * Note that the {@link GenFeature} may abort its addition if
     * {@link GenFeature#shouldApply(Species,
     * com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration)} returns {@code false}.
     *
     * @param feature the {@link GenFeature} to add
     * @return this {@link Species} object for chaining
     */
    public Species addGenFeature(GenFeature feature) {
        return this.addGenFeature(feature.getDefaultConfiguration());
    }

    /**
     * Adds the specified {@code configuration} to this species.
     * <p>
     * Note that the {@link GenFeature} can abort its addition if
     * {@link GenFeature#shouldApply(Species,
     * com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration)} returns {@code false}.
     *
     * @param configuration the configured gen feature to add
     * @return this {@link Species} object for chaining
     */
    public Species addGenFeature(GenFeatureConfiguration configuration) {
        if (configuration.shouldApply(this)) {
            this.genFeatures.add(configuration);
        } else {
            LogManager.getLogger().warn("Gen Feature \"{}\" refused to be applied to Species \"{}\".",
                    configuration.getGenFeature().getRegistryName(), getRegistryName());
        }
        return this;
    }

    public boolean hasGenFeatures() {
        return this.genFeatures.size() > 0;
    }

    public List<GenFeatureConfiguration> getGenFeatures() {
        return this.genFeatures;
    }

    /**
     * Allows the tree to prepare the area for planting.  For thick tree this may include removing blocks around the
     * trunk that could be in the way.
     *
     * @param world        The world
     * @param rootPosition The position of {@link RootyBlock} this tree will be planted in
     * @param radius       The radius of the generation area
     * @param facing       The direction the joCode will build the tree
     * @param safeBounds   An object that helps prevent accessing blocks in unloaded chunks
     * @param joCode       The joCode that will be used to grow the tree
     * @return new blockposition of root block.  BlockPos.ZERO to cancel generation
     */
    public BlockPos preGeneration(IWorld world, BlockPos rootPosition, int radius, Direction facing,
                                  SafeChunkBounds safeBounds, JoCode joCode) {
        final AtomicReference<BlockPos> rootPos = new AtomicReference<>(rootPosition);

        this.genFeatures.forEach(configuration -> rootPos.set(
                configuration.generate(
                        GenFeature.Type.PRE_GENERATION,
                        new PreGenerationContext(world, rootPos.get(), this, radius, facing, safeBounds, joCode)
                )
        ));

        return rootPos.get();
    }

    /**
     * Allows the tree to decorate itself after it has been generated. Use this to add vines, add fruit, fix the soil,
     * add butress roots etc.
     *
     * @param context The {@link PostGenerationContext} instance.
     */
    public void postGeneration(PostGenerationContext context) {
        this.genFeatures.forEach(configuration ->
                configuration.generate(GenFeature.Type.POST_GENERATION, context)
        );
    }

    /**
     * Worldgen can produce thin sickly trees from the underinflation caused by not living it's full fertility. This
     * factor is an attempt to compensate for the problem.
     *
     * @return
     */
    public float getWorldGenTaperingFactor() {
        return 1.5f;
    }

    private int worldGenLeafMapHeight = 32;

    public int getWorldGenLeafMapHeight() {
        return worldGenLeafMapHeight;
    }

    public void setWorldGenLeafMapHeight(int worldGenLeafMapHeight) {
        this.worldGenLeafMapHeight = worldGenLeafMapHeight;
    }

    public int getWorldGenAgeIterations() {
        return 3;
    }

    public NodeInspector getNodeInflator(SimpleVoxmap leafMap) {
        return new InflatorNode(this, leafMap);
    }

    /**
     * General purpose hashing algorithm using a {@link BlockPos} as an ingest.
     *
     * @param pos
     * @return hash for position
     */
    public int coordHashCode(BlockPos pos) {
        return CoordUtils.coordHashCode(pos, 2);
    }

    public boolean hasFruit(Fruit fruit) {
        return fruits.contains(fruit);
    }

    public boolean hasFruits() {
        return !fruits.isEmpty();
    }

    public void addFruits(Collection<Fruit> fruits) {
        this.fruits.addAll(fruits);
    }

    public Set<Fruit> getFruits() {
        return Collections.unmodifiableSet(fruits);
    }

    public boolean hasPod(Pod pod) {
        return pods.contains(pod);
    }

    public boolean hasPods() {
        return !pods.isEmpty();
    }

    public void addPods(Collection<Pod> pods) {
        this.pods.addAll(pods);
    }

    public Set<Pod> getPods() {
        return Collections.unmodifiableSet(pods);
    }

    public List<ITag.INamedTag<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.SAPLINGS);
    }

    public List<ITag.INamedTag<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.SEEDS);
    }

    /**
     * @return the location of the dynamic sapling smartmodel for this type of species
     */
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTrees.resLoc("block/smartmodel/sapling");
    }

    protected final MutableLazyValue<Generator<DTBlockStateProvider, Species>> saplingStateGenerator =
            MutableLazyValue.supplied(SaplingStateGenerator::new);

    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        textureConsumer.accept("particle", leavesTextureLocation);
        textureConsumer.accept("log", barkTextureLocation);
        textureConsumer.accept("leaves", leavesTextureLocation);
    }

    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        // Generate sapling block state and model.
        this.saplingStateGenerator.get().generate(provider, this);
    }

    /**
     * @return the location of the parent model of the seed item model
     */
    public ResourceLocation getSeedParentLocation() {
        return DynamicTrees.resLoc("item/standard_seed");
    }

    protected final MutableLazyValue<Generator<DTItemModelProvider, Species>> seedModelGenerator =
            MutableLazyValue.supplied(SeedItemModelGenerator::new);

    public Generator<DTItemModelProvider, Species> getSeedModelGenerator() {
        return this.seedModelGenerator.get();
    }

    @Override
    public void generateItemModelData(DTItemModelProvider provider) {
        // Generate seed models.
        this.seedModelGenerator.get().generate(provider, this);
    }

    public boolean shouldGenerateLeavesBlockDrops() {
        return shouldGenerateLeavesDrops();
    }

    private final LazyValue<ResourceLocation> leavesBlockDropsTableName = LazyValue.supplied(() -> {
        LeavesProperties leavesProperties = this.leavesProperties;
        if (leavesProperties == null) {
            leavesProperties = getCommonSpecies().leavesProperties;
        }
        return ResourceLocationUtils.suffix(getRegistryName(),
                (leavesProperties == null ? "_leaves" : leavesProperties.getBlockRegistryNameSuffix()));
    });

    private final LazyValue<ResourceLocation> leavesBlockDropsPath = LazyValue.supplied(() ->
            ResourceLocationUtils.prefix(leavesBlockDropsTableName.get(), "blocks/"));

    public ResourceLocation getLeavesBlockDropsPath() {
        return leavesBlockDropsPath.get();
    }

    public LootTable.Builder createLeavesBlockDrops() {
        if (getPrimitiveLeaves().isPresent() && seed != null) {
            return DTLootTableProvider.createLeavesBlockDrops(getPrimitiveLeaves().get(), this.seed, seedChances);
        } else if (seed == null) {
            return DTLootTableProvider.createLeavesBlockDrops(getPrimitiveLeaves().get());
        }
        return DTLootTableProvider.createLeavesDrops(seed, seedChances, LootParameterSets.BLOCK);
    }

    public boolean shouldGenerateLeavesDrops() {
        final boolean hasPrimitiveLeaves = getPrimitiveLeaves().isPresent();
        final boolean hasSeed = this.seed != null;
        return ((hasPrimitiveLeaves || hasSeed) && isCommonSpecies()) || (hasPrimitiveLeaves && hasSeed);
    }

    private final LazyValue<ResourceLocation> leavesDropsPath = LazyValue.supplied(() ->
            ResourceLocationUtils.prefix(getRegistryName(), "trees/leaves/"));

    public ResourceLocation getLeavesDropsPath() {
        return leavesDropsPath.get();
    }

    public LootTable.Builder createLeavesDrops() {
        if (seed == null) {
            return DTLootTableProvider.createLeavesDrops();
        }
        return DTLootTableProvider.createLeavesDrops(seed, seedChances, DTLootParameterSets.LEAVES);
    }

    public boolean shouldGenerateVoluntaryDrops() {
        return this.seed != null;
    }

    private final LazyValue<ResourceLocation> voluntaryDropsPath = LazyValue.supplied(() ->
            ResourceLocationUtils.prefix(getRegistryName(), "trees/voluntary/"));

    public ResourceLocation getVoluntaryDropsPath() {
        return voluntaryDropsPath.get();
    }

    public LootTable.Builder createVoluntaryDrops() {
        return DTLootTableProvider.createVoluntaryDrops(seed);
    }

    @Override
    public String toLoadDataString() {
        final RegistryHandler registryHandler = RegistryHandler.get(this.getRegistryName().getNamespace());
        return this.getString(Pair.of("seed", this.seed != null ? registryHandler.getRegName(this.seed) : null),
                Pair.of("sapling",
                        this.saplingBlock != null ? "Block{" + registryHandler.getRegName(this.saplingBlock) + "}" :
                                null));
    }

    @Override
    public String toReloadDataString() {
        return this.getString(Pair.of("tapering", this.tapering), Pair.of("upProbability", this.upProbability),
                Pair.of("lowestBranchHeight", this.lowestBranchHeight), Pair.of("signalEnergy", this.signalEnergy),
                Pair.of("growthRate", this.growthRate), Pair.of("soilLongevity", this.soilLongevity),
                Pair.of("soilTypeFlags", this.soilTypeFlags), Pair.of("maxBranchRadius", this.maxBranchRadius),
                Pair.of("transformable", this.transformable), Pair.of("logicKit", this.logicKit),
                Pair.of("leavesProperties", this.leavesProperties), Pair.of("envFactors", this.envFactors),
                Pair.of("megaSpecies", this.megaSpecies), Pair.of("seed", this.seed),
                Pair.of("primitive_sapling", TreeRegistry.SAPLING_REPLACERS.entrySet().stream()
                        .filter(entry -> entry.getValue() == this).map(Map.Entry::getKey).findAny()
                        .orElse(BlockStates.AIR)),
                Pair.of("perfectBiomes", this.perfectBiomes),
                Pair.of("acceptableBlocksForGrowth", this.acceptableBlocksForGrowth),
                Pair.of("genFeatures", this.genFeatures));
    }

}
