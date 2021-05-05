package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.*;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.event.BiomeSuitabilityEvent;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.models.FallingTreeEntityModel;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.dropcreators.*;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.nodemappers.*;
import com.ferreusveritas.dynamictrees.systems.substances.FertilizeSubstance;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.util.*;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class Species extends RegistryEntry<Species> implements IResettable<Species> {

	public static final Species NULL_SPECIES = new Species() {
		@Override public Optional<Seed> getSeed() { return Optional.empty(); }
		@Override public Family getFamily() { return Family.NULL_FAMILY; }
		@Override public boolean isTransformable() { return false; }
		@Override public boolean plantSapling(IWorld world, BlockPos pos) { return false; }
		@Override public boolean generate(World worldObj, IWorld world, BlockPos pos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) { return false; }
		@Override public float biomeSuitability(World world, BlockPos pos) { return 0.0f; }
		@Override public boolean addDropCreator(DropCreator dropCreator) { return false; }
		@Override public Species setSeed(Seed seed) { return this; }
		@Override public ItemStack getSeedStack(int qty) { return ItemStack.EMPTY; }
		@Override public Species setupStandardSeedDropping() { return this; }
		@Override public ITextComponent getTextComponent() { return this.getTextComponent("gui.none", TextFormatting.DARK_RED); }
		@Override public boolean update(World world, RootyBlock rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean rapid) { return false; }
	};

	public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(Species::new);

	public static TypedRegistry.EntryType<Species> createDefaultType(final Function3<ResourceLocation, Family, LeavesProperties, Species> constructor) {
		return TypedRegistry.newType(createDefaultCodec(constructor));
	}

	public static Codec<Species> createDefaultCodec(final Function3<ResourceLocation, Family, LeavesProperties, Species> constructor) {
		return RecordCodecBuilder.create(instance -> instance
				.group( ResourceLocation.CODEC.fieldOf(DTResourceRegistries.RESOURCE_LOCATION.toString())
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

	/** The family of tree this belongs to. E.g. "Oak" and "Swamp Oak" belong to the "Oak" Family */
	protected Family family = Family.NULL_FAMILY;
	
	/** Logic kit for standardized extended growth behavior */
	protected GrowthLogicKit logicKit = GrowthLogicKit.NULL_LOGIC;
	
	/** How quickly the branch thickens on it's own without branch merges [default = 0.3] */
	protected float tapering = 0.3f;
	/** The probability that the direction decider will choose up out of the other possible direction weights [default = 2] */
	protected int upProbability = 2;
	/** Number of blocks high we have to be before a branch is allowed to form [default = 3] (Just high enough to walk under) */
	protected int lowestBranchHeight = 3;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node [default = 16] */
	protected float signalEnergy = 16.0f;
	/** Ideal growth rate [default = 1.0] */
	protected float growthRate = 1.0f;
	/** Ideal soil longevity [default = 8] */
	protected int soilLongevity = 8;
	/** The tags for the types of soil the tree can be planted on */
	protected int soilTypeFlags = 0;

	// TODO: Make sure this is implemented properly.
	protected int maxBranchRadius = 8;

	/** Stores whether or not this species can be transformed to another, if {@code true} and
	 *  this species has it's own seed a transformation potion will also be automatically created. */
	private boolean transformable = true;

	/** If this is not empty, saplings will only grow when planted on these blocks. */
	protected final List<Block> acceptableBlocksForGrowth = Lists.newArrayList();

	//Leaves
	protected LeavesProperties leavesProperties = LeavesProperties.NULL_PROPERTIES;

	/** A list of leaf blocks the species accepts as its own. Used for the falling tree renderer */
	private final List<LeavesProperties> validLeaves = new LinkedList<>();
	
	//Seeds
	/** The seed used to reproduce this species.  Drops from the tree and can plant itself */
	protected Seed seed;
	/** Valid primitive sapling {@link Item}s. Used for dirt bucket recipes. */
	protected final Set<Item> primitiveSaplingItems = new HashSet<>();
	/** A blockState that will turn itself into this tree */
	protected DynamicSaplingBlock saplingBlock;
	/** A place to store what drops from the species. Similar to a loot table */
	protected StorageDropCreator dropCreatorStorage = new StorageDropCreator();
	
	//WorldGen
	/** A map of environmental biome factors that change a tree's suitability */
	protected Map<BiomeDictionary.Type, Float> envFactors = new HashMap<>();//Environmental factors

	protected List<Biome> perfectBiomes = new ArrayList<>();

	protected final List<ConfiguredGenFeature<?>> genFeatures = new ArrayList<>();

	/** A {@link BiPredicate} that returns true if this species should override the common in the given position. */
	protected ICommonOverride commonOverride;

	private String unlocalizedName = "";

	/**
	 * Blank constructor for {@link #NULL_SPECIES}.
	 */
	public Species() {
		this.setRegistryName(DTTrees.NULL);
	}

	/**
	 * Constructor suitable for derivative mods that defaults
	 * the leavesProperties to the common type for the family
	 *
	 * @param name The simple name of the species e.g. "oak"
	 * @param family The {@link Family} that this species belongs to.
	 */
	public Species(ResourceLocation name, Family family) {
		this(name, family, family.getCommonLeaves());
	}
	
	/**
	 * Constructor suitable for derivative mods
	 *
	 * @param name The simple name of the species e.g. "oak"
	 * @param leavesProperties The properties of the leaves to be used for this species
	 * @param family The {@link Family} that this species belongs to.
	 */
	public Species(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
		this.setRegistryName(name);
		this.setUnlocalizedName(name.toString());
		this.family = family;
		this.family.addSpecies(this);
		this.setLeavesProperties(leavesProperties.isValid() ? leavesProperties : family.getCommonLeaves());

		this.addDropCreator(new LogsDropCreator());
	}

	/**
	 * Resets this {@link Species} object's environment factors, gen features, acceptable
	 * blocks for growth, and acceptable soils. May also be overridden by sub-classes that
	 * need lists to be cleared on reload, for example.
	 *
	 * @return This {@link Species} object for chaining.
	 */
	@Override
	public Species reset () {
		this.envFactors.clear();
		this.genFeatures.clear();
		this.acceptableBlocksForGrowth.clear();
		this.primitiveSaplingItems.clear();

		this.clearAcceptableSoils();

		return this;
	}

	/**
	 * Can be overridden by sub-classes for setting defaults for things before reload,
	 * such as {@link #envFactors}.
	 *
	 * @return This {@link Species} object for chaining.
	 */
	@Override
	public Species setPreReloadDefaults() {
		return this.setDefaultGrowingParameters().setSaplingShape(CommonVoxelShapes.SAPLING).setSaplingSound(SoundType.GRASS);
	}

	/**
	 * Can be overridden by sub-classes for setting defaults for things after reload.
	 * This is for defaults like lists, and so defaults should only be set if there was
	 * nothing set by the Json.
	 *
	 * @return This {@link Species} object for chaining.
	 */
	@Override
	public Species setPostReloadDefaults() {
		// If there is no acceptable soil set, use the standard soils.
		if (!this.hasAcceptableSoil())
			this.setStandardSoils();
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
	 * Checks if this {@link Species} is the common species of its {@link Family}
	 * (it equals {@link Family#commonSpecies}).
	 *
	 * @return {@code true} if this species is the common of {@link #family};
	 *         {@code false} otherwise.
	 */
	public boolean isCommonSpecies() {
		return this.getCommonSpecies() == this;
	}

	/**
	 * Checks whether or not {@link #seed} is the same instance as the {@link Seed}
	 * of the common {@link Species} of the owning {@link Family}.
	 *
	 * @return {@code true} if {@link #seed} {@code ==} the {@link Seed} of the
	 *         common {@link Species} of {@link #family}; {@code false} otherwise.
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

	public ITextComponent getTextComponent() {
		return this.getTextComponent(this.getUnlocalizedName(), TextFormatting.AQUA);
	}

	protected ITextComponent getTextComponent(final String unlocalizedName, final TextFormatting colour) {
		return new TranslationTextComponent(unlocalizedName).withStyle(style -> style.withColor(colour)
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new TranslationTextComponent("chat.registry_name", this.getRegistryName())))
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
						this.getRegistryName().toString())));
	}

	public Species setBasicGrowingParameters(float tapering, float energy, int upProbability, int lowestBranchHeight, float growthRate) {
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

	public float getSignalEnergy(){ return signalEnergy; }

	public float getEnergy(World world, BlockPos rootPos) {
		return getGrowthLogicKit().getEnergy(world, rootPos, this, signalEnergy);
	}
	
	public float getGrowthRate(World world, BlockPos rootPos) {
		return this.growthRate * this.seasonalGrowthFactor(world, rootPos);
	}
	
	/** Probability reinforcer for up direction which is arguably the direction most trees generally grow in. */
	public int getUpProbability() {
		return upProbability;
	}
	
	/** Probability reinforcer for current travel direction */
	public int getReinfTravel() {
		return 1;
	}
	
	public int getLowestBranchHeight() {
		return lowestBranchHeight;
	}
	
	/**
	 * @param world
	 * @param pos
	 * @return The lowest number of blocks from the RootyDirtBlock that a branch can form.
	 */
	public int getLowestBranchHeight(World world, BlockPos pos) {
		return getGrowthLogicKit().getLowestBranchHeight(world, pos, this, lowestBranchHeight);
	}
	
	public float getTapering() {
		return tapering;
	}

	/**
	 * Works out if this {@link Species} will require a {@link SpeciesTileEntity} at the given position.
	 * It should require one if it's not the common species and it's not in its common species override for
	 * the given position.
	 *
	 * @param world The {@link IWorld} the tree is being planted in.
	 * @param pos The {@link BlockPos} at which the tree is being planted at.
	 * @return True if it will require a {@link SpeciesTileEntity}.
	 */
	public boolean doesRequireTileEntity(IWorld world, BlockPos pos) {
		return !this.isCommonSpecies() && !this.shouldOverrideCommon(world, pos);
	}

	/**
	 * Returns whether or not this species can be transformed to another. See {@link #transformable} for
	 * more details.
	 *
	 * @return True if it can be transformed to, false if not.
	 */
	public boolean isTransformable () {
		return this.transformable;
	}

	/**
	 * Sets whether or not this species can be transformed to another. See {@link #transformable} for
	 * more details.
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

	public void setCommonOverride(final ICommonOverride commonOverride) {
		this.commonOverride = commonOverride;
	}

	public boolean shouldOverrideCommon(final IWorld world, final BlockPos trunkPos) {
		return this.hasCommonOverride() && this.commonOverride.test(world, trunkPos);
	}

	public interface ICommonOverride extends BiPredicate<IWorld, BlockPos> {}

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

	public void addValidLeafBlocks (LeavesProperties... leaves){
		for (LeavesProperties leaf : leaves) {
			if (!this.validLeaves.contains(leaf))
				this.validLeaves.add(leaf);
		}
	}

	public int getLeafBlockIndex (DynamicLeavesBlock block){
		int index = validLeaves.indexOf(block.properties);
		if (index < 0) {
			LogManager.getLogger().warn("Block {} not valid leaves for {}.", block, this);
			return 0;
		}
		return index;
	}

	public DynamicLeavesBlock getValidLeafBlock (int index) {
		return (DynamicLeavesBlock) validLeaves.get(index).getDynamicLeavesState().getBlock();
	}

	public int colorTreeQuads (int defaultColor, FallingTreeEntityModel.TreeQuadData treeQuad){
		return defaultColor;
	}

	public int leafColorMultiplier (World world, BlockPos pos) {
		return getLeavesProperties().foliageColorMultiplier(getLeavesProperties().getDynamicLeavesState(), world, pos);
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
		return this.hasSeed() ? new ItemStack(this.seed, qty) : !this.isCommonSpecies() ? this.family.getCommonSpecies().getSeedStack(qty) : ItemStack.EMPTY;
	}
	
	public boolean hasSeed() {
		return this.seed != null;
	}
	
	public Optional<Seed> getSeed() {
		return !this.hasSeed() ? !this.isCommonSpecies() ? this.family.getCommonSpecies().getSeed() : Optional.empty() : Optional.of(this.seed);
	}

	/**
	 * Holds whether or not a {@link Seed} should be generated. Stored as a {@code non-primitive}
	 * so its default value is {@code null}.
	 */
	private Boolean shouldGenerateSeed;

	public boolean shouldGenerateSeed() {
		return this.shouldGenerateSeed != null && this.shouldGenerateSeed;
	}

	public void setShouldGenerateSeed(boolean shouldGenerateSeed) {
		this.shouldGenerateSeed = shouldGenerateSeed;
	}

	/**
	 * Sets {@link #shouldGenerateSeed} to the given boolean, only if it's currently
	 * {@code null}. This allows for setting a default which can then be overridden by
	 * Json.
	 *
	 * @param shouldGenerateSeed {@code true} if a seed should be generated;
	 *                           {@code false} otherwise.
	 * @return This {@link Species} object for chaining.
	 */
	public Species setShouldGenerateSeedIfNull(boolean shouldGenerateSeed) {
		if (this.shouldGenerateSeed == null)
			this.shouldGenerateSeed = shouldGenerateSeed;
		return this;
	}

	/**
	 * Generates and registers a {@link Seed} item for this species. Note that
	 * it will only be generated if {@link #shouldGenerateSeed} is {@code true}.
	 *
	 * @return This {@link Species} object for chaining.
	 */
	public Species generateSeed() {
		return !this.shouldGenerateSeed() || this.seed != null ? this :
				this.setSeed(RegistryHandler.addItem(ResourceLocationUtils.suffix(this.getRegistryName(), "_seed"), new Seed(this)));
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
	 * Sets up a standardized drop system for
	 * Harvest, Voluntary, and Leaves Drops.
	 *
	 * Typically called in the constructor
	 */
	public Species setupStandardSeedDropping() {
		addDropCreator(new SeedDropCreator());
		return this;
	}

	public Species setupStandardSeedDropping(float rarity) {
		addDropCreator(new SeedDropCreator(rarity));
		return this;
	}
	
	/**
	 * Same as setupStandardSeedDropping except it allows
	 * for a custom seed item.
	 */
	public Species setupCustomSeedDropping(ItemStack customSeed) {
		addDropCreator(new SeedDropCreator().setCustomSeedDrop(customSeed));
		return this;
	}

	public Species setupCustomSeedDropping(ItemStack customSeed, float rarity) {
		addDropCreator(new SeedDropCreator(rarity).setCustomSeedDrop(customSeed));
		return this;
	}

	public Species setupStandardStickDropping() {
		return this.setupStandardStickDropping(1);
	}

	public Species setupStandardStickDropping (float rarity) {
		this.addDropCreator(new SticksDropCreator(this, rarity, 2));
		return this;
	}

	public boolean addDropCreator(DropCreator dropCreator) {
		return dropCreatorStorage.addDropCreator(dropCreator);
	}
	
	public boolean remDropCreator(ResourceLocation dropCreatorName) {
		return dropCreatorStorage.remDropCreator(dropCreatorName);
	}
	
	public Map<ResourceLocation, DropCreator> getDropCreators() {
		return dropCreatorStorage.getDropCreators();
	}
	
	/**
	 * Gets a list of drops for a {@link DynamicLeavesBlock} when the entire tree is harvested.
	 * NOT used for individual {@link DynamicLeavesBlock} being directly harvested by hand or tool.
	 *
	 * @param world
	 * @param leafPos
	 * @param dropList
	 * @param random
	 * @return
	 */
	public List<ItemStack> getTreeHarvestDrops(World world, BlockPos leafPos, List<ItemStack> dropList, Random random) {
		final DropContext dropContext = new DropContext(world, leafPos, this, dropList);
 		TreeRegistry.GLOBAL_DROP_CREATOR_STORAGE.appendDrops(DropCreator.DropType.HARVEST, dropContext);
		return this.dropCreatorStorage.appendDrops(DropCreator.DropType.HARVEST, dropContext);
	}
	
	/**
	 * Gets a {@link List} of voluntary drops.  Voluntary drops are {@link ItemStack}s that fall from the {@link Family} at
	 * random with no player interaction.
	 *
	 * @param world
	 * @param rootPos
	 * @param treePos
	 * @param soilLife
	 * @return
	 */
	public List<ItemStack> getVoluntaryDrops(World world, BlockPos rootPos, BlockPos treePos, int soilLife) {
		final DropContext dropContext = new DropContext(world, rootPos, this, new ArrayList<>(), soilLife, 0);
		TreeRegistry.GLOBAL_DROP_CREATOR_STORAGE.appendDrops(DropCreator.DropType.VOLUNTARY, dropContext);
		return this.dropCreatorStorage.appendDrops(DropCreator.DropType.VOLUNTARY, dropContext);
	}
	
	/**
	 * Gets a {@link List} of Leaves drops.  Leaves drops are {@link ItemStack}s that result from the breaking of
	 * a {@link DynamicLeavesBlock} directly by hand or with a tool.
	 *
	 * @param world
	 * @param breakPos
	 * @param dropList
	 * @param fortune
	 * @return
	 */
	public List<ItemStack> getLeavesDrops(@Nullable World world, BlockPos breakPos, List<ItemStack> dropList, int fortune) {
		final DropContext dropContext = new DropContext(world, breakPos, this, dropList, -1, fortune);
		TreeRegistry.GLOBAL_DROP_CREATOR_STORAGE.appendDrops(DropCreator.DropType.LEAVES, dropContext);
		return dropCreatorStorage.appendDrops(DropCreator.DropType.LEAVES, dropContext);
	}
	
	
	/**
	 * Gets a {@link List} of Logs drops.  Logs drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BranchBlock} directly by hand or with a tool.
	 *
	 * @param world
	 * @param breakPos
	 * @param dropList
	 * @param volume
	 * @return
	 */
	public List<ItemStack> getLogsDrops(World world, BlockPos breakPos, List<ItemStack> dropList, NetVolumeNode.Volume volume, ItemStack handStack) {
		final LogDropContext dropContext = new LogDropContext(world, breakPos, this, dropList, volume);
		TreeRegistry.GLOBAL_DROP_CREATOR_STORAGE.appendDrops(DropCreator.DropType.LOGS, dropContext);
		return dropCreatorStorage.appendDrops(DropCreator.DropType.LOGS, dropContext);
	}
	
	public static class LogsAndSticks {
		public List<ItemStack> logs;
		public final int sticks;
		public LogsAndSticks(List<ItemStack> logs, int sticks) {
			this.logs = logs; this.sticks = DTConfigs.DROP_STICKS.get() ? sticks : 0;
		}
	}
	
	public LogsAndSticks getLogsAndSticks(NetVolumeNode.Volume volume) {
		List<ItemStack> logsList = new LinkedList<>();
		int[] volArray = volume.getRawVolumesArray();
		float prevVol = 0;
		for (int i=0; i< volArray.length; i++){
			float vol = (volArray[i] / (float) NetVolumeNode.Volume.VOXELSPERLOG);
			if (vol > 0){
				vol += prevVol;
				prevVol = getFamily().getValidBranchBlock(i).getPrimitiveLogs(vol, logsList);
			}
		}
		int sticks = (int)(prevVol * 8); // A stick is 1/8th of a log (1 log = 4 planks, 2 planks = 4 sticks) Give him the stick!
		return new LogsAndSticks(logsList, sticks);
	}
	
	/**
	 * @param world
	 * @param endPoints
	 * @param rootPos
	 * @param treePos
	 * @param soilLife
	 * @return true if seed was dropped
	 */
	public boolean handleVoluntaryDrops(World world, List<BlockPos> endPoints, BlockPos rootPos, BlockPos treePos, int soilLife) {
		int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		if(tickSpeed > 0) {
			double slowFactor = 3.0 / tickSpeed;//This is an attempt to normalize voluntary drop rates.
			if(world.random.nextDouble() < slowFactor) {
				List<ItemStack> drops = getVoluntaryDrops(world, rootPos, treePos, soilLife);
				
				if(!drops.isEmpty() && !endPoints.isEmpty()) {
					for(ItemStack drop: drops) {
						BlockPos branchPos = endPoints.get(world.random.nextInt(endPoints.size()));
						branchPos = branchPos.above();//We'll aim at the block above the end branch. Helps with Acacia leaf block formations
						BlockPos itemPos = CoordUtils.getRayTraceFruitPos(world, this, treePos, branchPos, SafeChunkBounds.ANY);

						if(itemPos != BlockPos.ZERO) {
							ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5, itemPos.getY() + 0.5, itemPos.getZ() + 0.5, drop);
							Vector3d motion = new Vector3d(itemPos.getX(), itemPos.getY(), itemPos.getZ()).subtract(new Vector3d(treePos.getX(), treePos.getY(), treePos.getZ()));
							float distAngle = 15;//The spread angle(center to edge)
							float launchSpeed = 4;//Blocks(meters) per second
							motion = new Vector3d(motion.x, 0, motion.y).normalize().yRot((world.random.nextFloat() * distAngle * 2) - distAngle).scale(launchSpeed/20f);
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

	public Set<Item> getPrimitiveSaplingItems() {
		final Set<Item> saplingItems = new HashSet<>(this.primitiveSaplingItems);
		saplingItems.addAll(TreeRegistry.SAPLING_REPLACERS.entrySet().stream().filter(entry -> entry.getValue() == this).map(Map.Entry::getKey)
				.map(BlockState::getBlock).map(Block::asItem).filter(item -> item != Items.AIR).collect(Collectors.toSet()));
		return saplingItems;
	}

	public Species addPrimitiveSaplingItem(final Item primitiveSaplingItem) {
		this.primitiveSaplingItems.add(primitiveSaplingItem);
		return this;
	}

	public Species setSapling(DynamicSaplingBlock sapling) {
		saplingBlock = sapling;
		return this;
	}

	/**
	 * Holds whether or not a {@link Seed} should be generated. Stored as a {@code non-primitive}
	 * so its default value is {@code null}.
	 */
	private Boolean shouldGenerateSapling;

	public boolean shouldGenerateSapling() {
		return this.shouldGenerateSapling != null && this.shouldGenerateSapling;
	}

	public void setShouldGenerateSapling(boolean shouldGenerateSapling) {
		this.shouldGenerateSapling = shouldGenerateSapling;
	}

	/**
	 * Sets {@link #shouldGenerateSapling} to the given boolean, only if it's currently
	 * {@code null}. This allows for setting a default which can then be overridden by
	 * Json.
	 *
	 * @param shouldGenerateSapling {@code true} if a sapling should be generated;
	 *                                          {@code false} otherwise.
	 * @return This {@link Species} object for chaining.
	 */
	public Species setShouldGenerateSaplingIfNull(boolean shouldGenerateSapling) {
		if (this.shouldGenerateSapling == null)
			this.shouldGenerateSapling = shouldGenerateSapling;
		return this;
	}

	/**
	 * Generates and registers a {@link DynamicLeavesBlock} for this species. Note that
	 * it will only be generated if {@link #shouldGenerateSapling} is {@code true}.
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
	 * Checks surroundings and places a dynamic sapling block.
	 *
	 * @param world
	 * @param pos
	 * @return true if the planting was successful
	 */
	public boolean plantSapling(IWorld world, BlockPos pos) {
		if(saplingBlock != null) {
			if(world.getBlockState(pos).getMaterial().isReplaceable() && DynamicSaplingBlock.canSaplingStay(world, this, pos)) {
				world.setBlock(pos, saplingBlock.defaultBlockState(), 3);
				return true;
			}
		}
		return false;
	}

	public void addAcceptableBlockForGrowth (final Block block) {
		this.acceptableBlocksForGrowth.add(block);
	}

	/**
	 * Checks if the sapling can grow at the given position.
	 *
	 * @param world The {@link World} object.
	 * @param pos The {@link BlockPos} the sapling is on.
	 * @return True if it can grow.
	 */
	public boolean canSaplingGrow(World world, BlockPos pos) {
		return this.acceptableBlocksForGrowth.isEmpty() || this.acceptableBlocksForGrowth.stream().anyMatch(block -> block == world.getBlockState(pos.below()).getBlock());
	}

	private boolean canSaplingGrowNaturally = true;

	public Species setCanSaplingGrowNaturally(boolean canSaplingGrowNaturally) {
		this.canSaplingGrowNaturally = canSaplingGrowNaturally;
		return this;
	}

	/**
	 * Determines whether or not the {@link #saplingBlock} should be able to grow
	 * without player intervention (bone-mealing).
	 *
	 * @param world The {@link World} instance.
	 * @param pos The {@link BlockPos} of the {@link DynamicSaplingBlock}.
	 * @return {@code true} if the sapling can and should grow naturally;
	 * 	       {@code false} otherwise.
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

	public int saplingFireSpread () { return 0; }
	public int saplingFlammability () { return 0; }

	public boolean transitionToTree(World world, BlockPos pos) {
		
		//Ensure planting conditions are right
		Family family = getFamily();
		if(world.isEmptyBlock(pos.above()) && isAcceptableSoil(world, pos.below(), world.getBlockState(pos.below()))) {
			family.getBranch().setRadius(world, pos, (int)family.getPrimaryThickness(), null);//set to a single branch with 1 radius
			world.setBlockAndUpdate(pos.above(), getLeavesProperties().getDynamicLeavesState());//Place a single leaf block on top
			placeRootyDirtBlock(world, pos.below(), 15);//Set to fully fertilized rooty dirt underneath
			
			if (doesRequireTileEntity(world, pos)) {
				SpeciesTileEntity speciesTE = DTRegistries.speciesTE.create();
				world.setBlockEntity(pos.below(), speciesTE);
				speciesTE.setSpecies(this);
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

	//This is used to load the sapling model
	public ResourceLocation getSaplingRegName() {
		return ResourceLocationUtils.suffix(this.getRegistryName(), "_sapling");
	}
	
	public int saplingColorMultiplier(BlockState state, IBlockDisplayReader access, BlockPos pos, int tintIndex) {
		return getLeavesProperties().foliageColorMultiplier(state, access, pos);
	}

	private SoundType saplingSound;
	
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

	public boolean placeRootyDirtBlock(IWorld world, BlockPos rootPos, int life) {
		Block dirt = world.getBlockState(rootPos).getBlock();

		if (!RootyBlockHelper.isBlockRegistered(dirt) && !(dirt instanceof RootyBlock)) {
			LogManager.getLogger().warn("Rooty Dirt block NOT FOUND for soil "+ dirt.getRegistryName()); //default to dirt and print error
			this.placeRootyDirtBlock(world, rootPos, Blocks.DIRT, life);
			return false;
		}

		if (dirt instanceof RootyBlock) {
			this.placeRootyDirtBlock(world, rootPos, (RootyBlock) dirt, life);
		} else if (RootyBlockHelper.isBlockRegistered(dirt)) {
			this.placeRootyDirtBlock(world, rootPos, dirt, life);
		}

		TileEntity tileEntity = world.getBlockEntity(rootPos);
		if (tileEntity instanceof SpeciesTileEntity) {
			SpeciesTileEntity speciesTE = (SpeciesTileEntity) tileEntity;
			speciesTE.setSpecies(this);
		}

		return true;
	}

	private void placeRootyDirtBlock (IWorld world, BlockPos rootPos, Block primitiveDirt, int life) {
		this.placeRootyDirtBlock(world, rootPos, RootyBlockHelper.getRootyBlock(primitiveDirt), life);
	}

	private void placeRootyDirtBlock (IWorld world, BlockPos rootPos, RootyBlock rootyBlock, int life) {
		world.setBlock(rootPos, rootyBlock.defaultBlockState().setValue(RootyBlock.FERTILITY, life).setValue(RootyBlock.IS_VARIANT, this.doesRequireTileEntity(world, rootPos)), 3);
	}
	
	public Species setSoilLongevity(int longevity) {
		soilLongevity = longevity;
		return this;
	}
	
	public int getSoilLongevity(World world, BlockPos rootPos) {
		return (int)(biomeSuitability(world, rootPos) * soilLongevity);
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

	public Species addAcceptableSoils(String ... soilTypes) {
		soilTypeFlags |= DirtHelper.getSoilFlags(soilTypes);
		return this;
	}

	/**
	 * Will clear the acceptable soils list.  Useful for
	 * making trees that can only be planted in abnormal
	 * substrates.
	 */
	public Species clearAcceptableSoils() {
		soilTypeFlags = 0;
		return this;
	}

	/**
	 * This is run by the Species class itself to set the standard
	 * blocks available to be used as planting substrate. Developer
	 * may override this entirely or just modify the list at a
	 * later time.
	 */
	protected void setStandardSoils() {
		addAcceptableSoils(DirtHelper.DIRT_LIKE);
	}

	public boolean hasAcceptableSoil () {
		return this.soilTypeFlags != 0;
	}

	/**
	 * Soil acceptability tester.  Mostly to test if the block is dirt but could
	 * be overridden to allow gravel, sand, or whatever makes sense for the tree
	 * species.
	 *
	 * @param soilBlockState
	 * @return
	 */
	public boolean isAcceptableSoil(BlockState soilBlockState) {
		return DirtHelper.isSoilAcceptable(soilBlockState.getBlock(), soilTypeFlags);
	}

	/**
	 * Position sensitive soil acceptability tester.  Mostly to test if the block is dirt but could
	 * be overridden to allow gravel, sand, or whatever makes sense for the tree
	 * species.
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
	protected boolean isWater (BlockState soilBlockState){
		return DirtHelper.isSoilAcceptable(soilBlockState.getBlock(), DirtHelper.getSoilFlags(DirtHelper.WATER_LIKE));
	}
	
	
	//////////////////////////////
	// GROWTH
	//////////////////////////////
	
	/**
	 * Basic update. This handles everything for the species Rot, Drops, Fruit, Disease, and Growth respectively.
	 * If the rapid option is enabled then drops, fruit and disease are skipped.
	 *
	 * This should never be run by the world generator.
	 *
	 *
	 * @param world The world
	 * @param rootyDirt The {@link RootyBlock} that is supporting this tree
	 * @param rootPos The {@link BlockPos} of the {@link RootyBlock} type in the world
	 * @param soilLife The life of the soil. 0: Depleted -> 15: Full
	 * @param treePos The {@link BlockPos} of the {@link Family} trunk base.
	 * @param random A random number generator
	 * @param natural Set this to true if this member is being used to naturally grow the tree(create drops or fruit)
	 * @return true if network is viable.  false if network is not viable(will destroy the {@link RootyBlock} this tree is on)
	 */
	public boolean update(World world, RootyBlock rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean natural) {
		
		//Analyze structure to gather all of the endpoints.  They will be useful for this entire update
		List<BlockPos> ends = getEnds(world, treePos, treeBase);
		
		//This will prune rotted positions from the world and the end point list
		if(handleRot(world, ends, rootPos, treePos, soilLife, SafeChunkBounds.ANY)) {
			return false;//Last piece of tree rotted away.
		}
		
		if(natural) {
			//This will handle seed drops
			handleVoluntaryDrops(world, ends, rootPos, treePos, soilLife);
			
			//This will handle disease chance
			if(handleDisease(world, treeBase, treePos, random, soilLife)) {
				return true;//Although the tree may be diseased. The tree network is still viable.
			}
		}
		
		return grow(world, rootyDirt, rootPos, soilLife, treeBase, treePos, random, natural);
	}
	
	/**
	 * A little internal convenience function for getting branch endpoints
	 *
	 * @param world The world
	 * @param treePos The {@link BlockPos} of the base of the {@link Family} trunk
	 * @param treeBase The tree part that is the base of the {@link Family} trunk.  Provided for easy analysis.
	 * @return A list of all branch endpoints for the {@link Family}
	 */
	final protected List<BlockPos> getEnds(World world, BlockPos treePos, ITreePart treeBase) {
		FindEndsNode endFinder = new FindEndsNode();
		treeBase.analyse(world.getBlockState(treePos), world, treePos, null, new MapSignal(endFinder));
		return endFinder.getEnds();
	}
	
	/**
	 * A postRot handler.
	 *
	 * @param world The world
	 * @param ends A {@link List} of {@link BlockPos}s of {@link BranchBlock} endpoints.
	 * @param rootPos The {@link BlockPos} of the {@link RootyBlock} for this {@link Family}
	 * @param treePos The {@link BlockPos} of the trunk base for this {@link Family}
	 * @param soilLife The soil life of the {@link RootyBlock}
	 * @param safeBounds The defined boundaries where it is safe to make block changes
	 * @return true if last piece of tree rotted away.
	 */
	public boolean handleRot(IWorld world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {
		
		Iterator<BlockPos> iter = ends.iterator();//We need an iterator since we may be removing elements.
		SimpleVoxmap leafMap = getLeavesProperties().getCellKit().getLeafCluster();
		
		while (iter.hasNext()) {
			BlockPos endPos = iter.next();
			BlockState branchState = world.getBlockState(endPos);
			BranchBlock branch = TreeHelper.getBranch(branchState);
			if(branch != null) {
				int radius = branch.getRadius(branchState);
				float rotChance = rotChance(world, endPos, world.getRandom(), radius);
				if(branch.checkForRot(world, endPos, this, radius, world.getRandom(), rotChance, safeBounds != SafeChunkBounds.ANY) || radius != family.getPrimaryThickness()) {
					if(safeBounds != SafeChunkBounds.ANY) { //worldgen
						TreeHelper.ageVolume(world, endPos.below((leafMap.getLenZ() - 1) / 2), (leafMap.getLenX() - 1) / 2, leafMap.getLenY(), 2, safeBounds);
					}
					iter.remove();//Prune out the rotted end points so we don't spawn fruit from them.
				}
			}
		}
		
		return ends.isEmpty() && !TreeHelper.isBranch(world.getBlockState(treePos));//There are no endpoints and the trunk is missing
	}
	
	static private final Direction[] upFirst = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	/**
	 * Handle rotting branches
	 *
	 * @param world The world
	 * @param pos The {@link BlockPos}.
	 * @param neighborCount Count of neighbors reinforcing this block
	 * @param radius The radius of the branch
	 * @param random Access to a random number generator
	 * @param rapid True if this rot is happening under a generation scenario as opposed to natural tree updates
	 * @return true if the branch should rot
	 */
	public boolean rot(IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
		if (radius <= family.getPrimaryThickness()) {
			DynamicLeavesBlock leaves = (DynamicLeavesBlock) getLeavesProperties().getDynamicLeavesState().getBlock();
			for (Direction dir: upFirst) {
				if (leaves.growLeavesIfLocationIsSuitable(world, getLeavesProperties(), pos.relative(dir), 0)) {
					return false;
				}
			}
		}
		
		if (rapid || (DTConfigs.MAX_BRANCH_ROT_RADIUS.get() != 0 && radius <= DTConfigs.MAX_BRANCH_ROT_RADIUS.get())) {
			BranchBlock branch = TreeHelper.getBranch(world.getBlockState(pos));
			if (branch != null) {
				branch.rot(world, pos);
			}
			this.postRot(world, pos, neighborCount, radius, random, rapid);
			return true;
		}
		
		return false;
	}

	private void postRot(final IWorld world, final BlockPos pos, final int neighborCount, final int radius, final Random random, final boolean rapid) {
		this.genFeatures.stream().filter(configuredGenFeature -> configuredGenFeature.getGenFeature() instanceof IPostRotGenFeature).forEach(configuredGenFeature ->
				((IPostRotGenFeature) configuredGenFeature.getGenFeature()).postRot(configuredGenFeature, world, pos, neighborCount, radius, random, rapid));
	}
	
	/**
	 * Provides the chance that a log will postRot.
	 *
	 * @param world The world
	 * @param pos The {@link BlockPos} of the {@link BranchBlock}
	 * @param rand A random number generator
	 * @param radius The radius of the {@link BranchBlock}
	 * @return The chance this will postRot. 0.0(never) -> 1.0(always)
	 */
	public float rotChance(IWorld world, BlockPos pos, Random rand, int radius) {
		return 0.3f + ((8 - radius) * 0.1f);// Thicker branches take longer to postRot
	}
	
	/**
	 * The grow handler.
	 *
	 * @param world The world
	 * @param rootyDirt The {@link RootyBlock} that is supporting this tree
	 * @param rootPos The {@link BlockPos} of the {@link RootyBlock} type in the world
	 * @param soilLife The life of the soil. 0: Depleted -> 15: Full
	 * @param treePos The {@link BlockPos} of the {@link Family} trunk base.
	 * @param random A random number generator
	 * @param natural
	 * 		If true then this member is being used to grow the tree naturally(create drops or fruit).
	 *      If false then this member is being used to grow a tree with a growth accelerant like bonemeal or the potion of burgeoning
	 * @return true if network is viable.  false if network is not viable(will destroy the {@link RootyBlock} this tree is on)
	 */
	public boolean grow(World world, RootyBlock rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean natural) {
		
		float growthRate = (float) (getGrowthRate(world, rootPos) * DTConfigs.TREE_GROWTH_MULTIPLIER.get() * DTConfigs.TREE_GROWTH_FOLDING.get());
		do {
			if (soilLife > 0) {
				if (growthRate > random.nextFloat()) {
					final GrowSignal signal = new GrowSignal(this, rootPos, getEnergy(world, rootPos), world.random);
					boolean success = treeBase.growSignal(world, treePos, signal).success;
					
					int soilLongevity = getSoilLongevity(world, rootPos) * (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed
					
					if (soilLongevity <= 0 || random.nextInt(soilLongevity) == 0) {//1 in X(soilLongevity) chance to draw nutrients from soil
						rootyDirt.setSoilLife(world, rootPos, soilLife - 1);//decrement soil life
					}
					
					if (signal.choked) {
						soilLife = 0;
						rootyDirt.setSoilLife(world, rootPos, soilLife);
						TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new ShrinkerNode(signal.getSpecies())));
					}
				}
			}
		} while(--growthRate > 0.0f);
		
		return postGrow(world, rootPos, treePos, soilLife, natural);
	}
	
	/**
	 * Set the logic kit used to determine how the tree branch network expands.
	 * Provides an alternate and more modular method to override a trees
	 * growth logic.
	 *
	 * @param logicKit A growth logic kit
	 * @return this species for chaining
	 */
	public Species setGrowthLogicKit(GrowthLogicKit logicKit) {
		this.logicKit = logicKit;
		return this;
	}
	
	public GrowthLogicKit getGrowthLogicKit() {
		return logicKit;
	}

	public boolean canBoneMealTree() {
		return true;
	}

	/**
	 * Selects a new direction for the branch(grow) signal to turn to.
	 *
	 * <p>This function uses a probability map to make the decision and is acted upon by the
	 * GrowSignal() function in the branch block. Can be overridden for different species but
	 * it's preferable to override customDirectionManipulation.</p>
	 *
	 * @param world The {@link World} object.
	 * @param pos The {@link BlockPos} of the branch.
	 * @param branch The branch block the GrowSignal is traveling in.
	 * @param signal The grow signal.
	 * @return The selected {@link Direction}.
	 */
	public Direction selectNewDirection(World world, BlockPos pos, BranchBlock branch, GrowSignal signal) {
		Direction originDir = signal.dir.getOpposite();
		
		// prevent branches on the ground
		if(signal.numSteps + 1 <= getLowestBranchHeight(world, signal.rootPos)) {
			return Direction.UP;
		}
		
		int[] probMap = new int[6]; // 6 directions possible DUNSWE
		
		// Probability taking direction into account
		probMap[Direction.UP.ordinal()] = signal.dir != Direction.DOWN ? getUpProbability(): 0; // Favor up
		probMap[signal.dir.ordinal()] += getReinfTravel(); // Favor current direction
		
		// Create probability map for direction change
		for(Direction dir: Direction.values()) {
			if(!dir.equals(originDir)) {
				BlockPos deltaPos = pos.relative(dir);
				// Check probability for surrounding blocks
				// Typically Air:1, Leaves:2, Branches: 2+r
				BlockState deltaBlockState = world.getBlockState(deltaPos);
				probMap[dir.get3DDataValue()] += TreeHelper.getTreePart(deltaBlockState).probabilityForBlock(deltaBlockState, world, deltaPos, branch);
			}
		}
		
		// Do custom stuff or override probability map for various species
		probMap = customDirectionManipulation(world, pos, branch.getRadius(world.getBlockState(pos)), signal, probMap);
		
		int choice = com.ferreusveritas.dynamictrees.util.MathHelper.selectRandomFromDistribution(signal.rand, probMap); // Select a direction from the probability map.
		return newDirectionSelected(Direction.values()[choice != -1 ? choice : 1], signal); // Default to up if things are screwy
	}
	
	/** Species can override the probability map here **/
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int[] probMap) {
		return getGrowthLogicKit().directionManipulation(world, pos, this, radius, signal, probMap);
	}
	
	/** Species can override to take action once a new direction is selected **/
	protected Direction newDirectionSelected(Direction newDir, GrowSignal signal) {
		return getGrowthLogicKit().newDirectionSelected(this, newDir, signal);
	}
	
	/**
	 * Allows a species to do things after a grow event just occurred. Such as used
	 * by Jungle trees to create cocoa pods on the trunk.
	 *
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block
	 * @param treePos The position of the base trunk block of the tree(usually directly above the rooty dirt block)
	 * @param soilLife The life of the soil block this tree is planted in
	 * @param natural
	 * 		If true then this member is being used to grow the tree naturally (create drops or fruit).
	 * 		If false then this member is being used to grow a tree with a growth accelerant like bonemeal or the potion of burgeoning.
	 */
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean natural) {
		for (final ConfiguredGenFeature<?> configuredGenFeature : this.genFeatures) {
			final GenFeature genFeature = configuredGenFeature.getGenFeature();

			if (!(genFeature instanceof IPostGrowFeature))
				continue;

			((IPostGrowFeature) genFeature).postGrow(configuredGenFeature, world, rootPos, treePos, this, soilLife, natural);
		}

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
	public boolean handleDisease(World world, ITreePart baseTreePart, BlockPos treePos, Random random, int soilLife) {
		if (soilLife == 0 && DTConfigs.DISEASE_CHANCE.get() > random.nextFloat() ) {
			baseTreePart.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN, new MapSignal(new DiseaseNode(this)));
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
	 *
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
		
		float ugs = (float)(double) DTConfigs.SCALE_BIOME_GROWTH_RATE.get(); // Universal growth scalar.
		
		if (ugs == 1.0f || this.isBiomePerfect(biome)) {
			return 1.0f;
		}
		
		float suit = defaultSuitability();
		
		for (BiomeDictionary.Type t : BiomeDictionary.getTypes(RegistryKey.create(net.minecraft.util.registry.Registry.BIOME_REGISTRY, biome.getRegistryName()))) {
			suit *= envFactors.getOrDefault(t, 1.0f);
		}
		
		//Linear interpolation of suitability with universal growth scalar
		suit = ugs <= 0.5f ? ugs * 2.0f * suit : ((1.0f - ugs) * suit + (ugs - 0.5f)) * 2.0f;
		
		return MathHelper.clamp(suit, 0.0f, 1.0f);
	}

	/**
	 * Used to determine if the provided {@link Biome} argument will yield
	 * unhindered growth to Maximum potential. This has the affect of the
	 * suitability being 100%(or 1.0f)
	 *
	 * @param biome The biome being tested
	 * @return True if biome is "perfect" false otherwise.
	 */
	public boolean isBiomePerfect(final Biome biome) {
		return this.perfectBiomes.contains(biome);
	}

	/**
	 * Used to determine if the provided {@link Biome} argument will yield
	 * unhindered growth to Maximum potential. This has the affect of the
	 * suitability being 100%(or 1.0f)
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

	public static Biome getBiome (final RegistryKey<Biome> biomeKey) {
		return Objects.requireNonNull(ForgeRegistries.BIOMES.getValue(biomeKey.getRegistryName()));
	}

	public static RegistryKey<Biome> getBiomeKey (final Biome biome) {
		return RegistryKey.create(net.minecraft.util.registry.Registry.BIOME_REGISTRY, Objects.requireNonNull(biome.getRegistryName()));
	}
	
	/** A value that determines what a tree's suitability is before climate manipulation occurs. */
	public static float defaultSuitability() {
		return 0.85f;
	}
	
	/**
	 * A convenience function to test if a biome is one of the many options passed.
	 *
	 * @param biomeToCheck The biome we are matching
	 * @param biomes Multiple biomes to match against
	 * @return True if a match is found. False if not.
	 */
	@SafeVarargs
	public static boolean isOneOfBiomes(RegistryKey<Biome> biomeToCheck, RegistryKey<Biome>... biomes) {
		for (RegistryKey<Biome> biome: biomes) {
			if (biomeToCheck.equals(biome)) {
				return true;
			}
		}
		return false;
	}


	//////////////////////////////
	// SEASONAL
	//////////////////////////////

	protected float flowerSeasonHoldMin = SeasonHelper.SPRING;
	protected float flowerSeasonHoldMax = SeasonHelper.SPRING + 0.5f;

	/**
	 * Pulls data from the {@link com.ferreusveritas.dynamictrees.compat.seasons.SeasonManager} to determine
	 * the rate of tree growth for the current season.
	 *
	 * @param world The {@link World} object.
	 * @param rootPos the {@link BlockPos} of the {@link RootyBlock}.
	 * @return Factor from 0.0 (no growth) to 1.0 (full growth).
	 */
	public float seasonalGrowthFactor(World world, BlockPos rootPos) {
		return DTConfigs.ENABLE_SEASONAL_GROWTH_FACTOR.get() ? SeasonHelper.globalSeasonalGrowthFactor(world, rootPos) : 1.0f;
	}

	public float seasonalSeedDropFactor(World world, BlockPos pos) {
		return DTConfigs.ENABLE_SEASONAL_SEED_DROP_FACTOR.get() ? SeasonHelper.globalSeasonalSeedDropFactor(world, pos) : 1.0f;
	}

	public float seasonalFruitProductionFactor(World world, BlockPos pos) {
		return DTConfigs.ENABLE_SEASONAL_FRUIT_PRODUCTION_FACTOR.get() ? SeasonHelper.globalSeasonalFruitProductionFactor(world, pos, false) : 1.0f;
	}

	/**
	 * 1 = Spring
	 * 2 = Summer
	 * 4 = Autumn
	 * 8 = Winter
	 * Values are OR'ed together for the return
	 */
	public int getSeasonalTooltipFlags(final World world) {
		final float seasonStart = 0.167f;
		final float seasonEnd = 0.833f;
		final float threshold = 0.75f;

		if (FruitBlock.getFruitBlockForSpecies(this) != null) {
			int seasonFlags = 0;
			for (int i = 0; i < 4; i++) {
				final float prod1 = SeasonHelper.globalSeasonalFruitProductionFactor(world, new BlockPos(0, (int) ((i + seasonStart) * 64.0f), 0), true);
				final float prod2 = SeasonHelper.globalSeasonalFruitProductionFactor(world, new BlockPos(0, (int) ((i + seasonEnd  ) * 64.0f), 0), true);

				if (Math.min(prod1, prod2) >= threshold) {
					seasonFlags |= 1 << i;
				}
			}
			return seasonFlags;
		}

		return 0;
	}

	/**
	 * When seasons are active allow a seasonal time range where fruit growth does
	 * not progress past the flower stage. This allows for a flowery spring time.
	 *
	 * @param min The minimum season value.
	 * @param max The maximum season value.
	 * @return This {@link Species} object for chaining.
	 */
	public Species setFlowerSeasonHold(float min, float max) {
		flowerSeasonHoldMin = min;
		flowerSeasonHoldMax = max;
		return this;
	}

	public boolean testFlowerSeasonHold(Float seasonValue) {
		return SeasonHelper.isSeasonBetween(seasonValue, flowerSeasonHoldMin, flowerSeasonHoldMax);
	}


	//////////////////////////////
	// INTERACTIVE
	//////////////////////////////

	@Nullable
	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {
		
		// Bonemeal fertilizes the soil and causes a single growth pulse
		if (canBoneMealTree() && itemStack.getItem() == Items.BONE_MEAL) {
			return new FertilizeSubstance().setAmount(1).setGrow(true).setPulses(DTConfigs.BONE_MEAL_GROWTH_PULSES::get);
		}
		
		// Use substance provider interface if it's available
		if (itemStack.getItem() instanceof ISubstanceEffectProvider) {
			ISubstanceEffectProvider provider = (ISubstanceEffectProvider) itemStack.getItem();
			return provider.getSubstanceEffect(itemStack);
		}
		
		return null;
	}
	
	/**
	 * Apply an item to the treepart(e.g. bonemeal). Developer is responsible for decrementing itemStack after applying.
	 *
	 * @param world The current world
	 * @param hitPos Position
	 * @param player The player applying the substance
	 * @param itemStack The itemstack to be used.
	 * @return true if item was used, false otherwise
	 */
	public boolean applySubstance(World world, BlockPos rootPos, BlockPos hitPos, PlayerEntity player, Hand hand, ItemStack itemStack) {
		final ISubstanceEffect effect = getSubstanceEffect(itemStack);
		
		if (effect != null) {
			if (effect.isLingering()) {
				world.addFreshEntity(new LingeringEffectorEntity(world, rootPos, effect));
				return true;
			} else {
				return effect.apply(world, rootPos);
			}
		}
		
		return false;
	}
	
	/**
	 * Called when a player right clicks a {@link Species} of tree anywhere on it's branches.
	 *
	 * @param world The world
	 * @param rootPos The  {@link BlockPos} of the {@link RootyBlock}
	 * @param hitPos The {@link BlockPos} of the {@link Block} that was hit.
	 * @param state The {@link BlockState} of the hit {@link Block}.
	 * @param player The {@link PlayerEntity} that hit the {@link Block}
	 * @param hand Hand used to peform the action
	 * @param heldItem The {@link ItemStack} the {@link PlayerEntity} hit the {@link Block} with.
	 * @param hit The block ray trace of the clicking action
	 * @return True if action was handled, false otherwise.
	 */
	public boolean onTreeActivated(World world, BlockPos rootPos, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, @Nullable ItemStack heldItem, BlockRayTraceResult hit) {
		
		if (heldItem != null) { // Ensure there is something in the player's hand.
			if (applySubstance(world, rootPos, hitPos, player, hand, heldItem)) {
				consumePlayerItem(player, hand, heldItem);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * A convenience function to decrement or otherwise consume an item in use.
	 *
	 * @param player The player
	 * @param hand Hand holding the item
	 * @param heldItem The item to be consumed
	 */
	public static void consumePlayerItem(PlayerEntity player, Hand hand, ItemStack heldItem) {
		if (!player.isCreative()) {
			if (heldItem.getItem() instanceof IEmptiable) { // A substance deployed from a refillable container.
				final IEmptiable emptiable = (IEmptiable) heldItem.getItem();
				player.setItemInHand(hand, emptiable.getEmptyContainer());
			} else if(heldItem.getItem() == Items.POTION) { // An actual potion.
				player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
			} else {
				heldItem.shrink(1); // Just a regular item like bonemeal.
			}
		}
	}

	/**
	 * The Waila body is the part of the Waila display that shows the species and log/stick count
	 *
	 * @return true if the tree uses the default Waila body display. False if it has a custom one (disabling DT's display)
	 */
	public boolean useDefaultWailaBody (){
		return true;
	}

	protected boolean alwaysShowOnWaila;

	public boolean alwaysShowOnWaila() {
		return this.alwaysShowOnWaila;
	}

	public Species alwaysShowOnWaila(final boolean alwaysShowOnWaila) {
		this.alwaysShowOnWaila = alwaysShowOnWaila;
		return this;
	}

	public boolean showSpeciesOnWaila (){
		return this.alwaysShowOnWaila || this != getFamily().getCommonSpecies();
	}
	
	///////////////////////////////////////////
	// MEGANESS
	///////////////////////////////////////////

	private Species megaSpecies = Species.NULL_SPECIES;
	private boolean isMegaSpecies = false;

	public Species getMegaSpecies() {
		return this.megaSpecies;
	}

	public boolean isMegaSpecies() { return isMegaSpecies; }

	public void setMegaSpecies(final Species megaSpecies) {
		this.megaSpecies = megaSpecies;
		megaSpecies.isMegaSpecies = true;
	}

	///////////////////////////////////////////
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////
	
	public IAnimationHandler selectAnimationHandler(FallingTreeEntity fallingEntity) {
		return getFamily().selectAnimationHandler(fallingEntity);
	}

	/**
	 * This is used for trees that have leaves that are not cubes and require extra blockstate properties such as palm fronds.
	 * Used for tree felling animation.
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
	 * Provides the {@link PottedSaplingBlock} for this Species. {@link Species} subclasses can
	 * derive their own {@link PottedSaplingBlock} subclass if they want something custom.
	 *
	 * @return The {@link PottedSaplingBlock} for this {@link Species}.
	 */
	public PottedSaplingBlock getBonsaiPot() {
		return DTRegistries.POTTED_SAPLING;
	}
	
	
	//////////////////////////////
	// WORLDGEN
	//////////////////////////////
	
	/**
	 * Default worldgen spawn mechanism.
	 * This method uses JoCodes to generate tree models.
	 * Override to use other methods.
	 *
	 * @param world The world
	 * @param rootPos The position of {@link RootyBlock} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param radius The radius of the tree generation boundary
	 * @return true if tree was generated. false otherwise.
	 */
	public boolean generate(World worldObj, IWorld world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		for (ConfiguredGenFeature<?> configuredGenFeature : this.genFeatures) {
			final GenFeature genFeature = configuredGenFeature.getGenFeature();
			if (genFeature instanceof IFullGenFeature) {
				return ((IFullGenFeature) genFeature).generate(configuredGenFeature, world, rootPos, this, biome, random, radius, safeBounds);
			}
		}

		final Direction facing = CoordUtils.getRandomDir(random);
		if (!DTResourceRegistries.JO_CODE_MANAGER.getCodes(this).isEmpty()) {
			final JoCode code = DTResourceRegistries.JO_CODE_MANAGER.getRandomCode(this, radius, random);
			if (code != null) {
				code.generate(worldObj, world,this, rootPos, biome, facing, radius, safeBounds);
				return true;
			}
		}
		
		return false;
	}

	public JoCode getJoCode(String joCodeString) {
		return new JoCode(joCodeString);
	}

	public Collection<JoCode> getJoCodes() {
		return DTResourceRegistries.JO_CODE_MANAGER.getCodes(this).values().stream().flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Adds the default configuration of the {@link GenFeature} given.
	 *
	 * @param feature The {@link GenFeature} to add.
	 * @return This {@link Species} object.
	 */
	public Species addGenFeature(GenFeature feature) {
		return this.addGenFeature(feature.getDefaultConfiguration());
	}

	/**
	 * Adds a {@link ConfiguredGenFeature} object to this species.
	 *
	 * @param configuredGenFeature The {@link ConfiguredGenFeature} to add.
	 * @return This {@link Species} object.
	 */
	public Species addGenFeature(ConfiguredGenFeature<?> configuredGenFeature) {
		this.genFeatures.add(configuredGenFeature);
		return this;
	}

	public boolean areAnyGenFeatures () {
		return this.genFeatures.size() > 0;
	}

	/**
	 * Allows the tree to prepare the area for planting.  For thick tree this may include removing blocks around the trunk that
	 * could be in the way.
	 *
	 * @param world The world
	 * @param rootPos The position of {@link RootyBlock} this tree will be planted in
	 * @param radius The radius of the generation area
	 * @param facing The direction the joCode will build the tree
	 * @param safeBounds An object that helps prevent accessing blocks in unloaded chunks
	 * @param joCode The joCode that will be used to grow the tree
	 * @return new blockposition of root block.  BlockPos.ZERO to cancel generation
	 */
	public BlockPos preGeneration(IWorld world, BlockPos rootPos, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode) {
		for (ConfiguredGenFeature<?> configuredGenFeature : this.genFeatures) {
			GenFeature genFeature = configuredGenFeature.getGenFeature();

			if (!(genFeature instanceof IPreGenFeature)) continue;

			rootPos = ((IPreGenFeature) genFeature).preGeneration(configuredGenFeature, world, rootPos, this, radius, facing, safeBounds, joCode);
		}

		return rootPos;
	}
	
	/**
	 * Allows the tree to decorate itself after it has been generated.
	 * Use this to add vines, add fruit, fix the soil, add butress roots etc.
	 *
	 * @param worldObj The world object
	 * @param world The world object.
	 * @param rootPos The position of {@link RootyBlock} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param radius The radius of the tree generation boundary
	 * @param endPoints A {@link List} of {@link BlockPos} in the world designating branch endpoints
	 * @param safeBounds An object that helps prevent accessing blocks in unloaded chunks
	 * @param initialDirtState The blockstate of the dirt that became rooty.  Useful for matching terrain.
	 */
	public void postGeneration(World worldObj, IWorld world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		for (ConfiguredGenFeature<?> configuredGenFeature : this.genFeatures) {
			GenFeature genFeature = configuredGenFeature.getGenFeature();

			if (!(genFeature instanceof IPostGenFeature)) continue;

			((IPostGenFeature) genFeature).postGeneration(configuredGenFeature, world, rootPos, this, biome, radius, endPoints, safeBounds,
					initialDirtState, SeasonHelper.getSeasonValue(worldObj, rootPos), this.seasonalFruitProductionFactor(worldObj, rootPos));
		}
	}
	
	/**
	 * Worldgen can produce thin sickly trees from the underinflation caused by not living it's full life.
	 * This factor is an attempt to compensate for the problem.
	 *
	 * @return
	 */
	public float getWorldGenTaperingFactor() {
		return 1.5f;
	}
	
	public int getWorldGenLeafMapHeight() {
		return 32;
	}
	
	public int getWorldGenAgeIterations() {
		return 3;
	}
	
	public INodeInspector getNodeInflator(SimpleVoxmap leafMap) {
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

	@Override
	public String toLoadDataString() {
		final RegistryHandler registryHandler = RegistryHandler.get(this.getRegistryName().getNamespace());
		return this.getString(Pair.of("seed", this.seed != null ? registryHandler.getRegName(this.seed) : null),
				Pair.of("sapling", this.saplingBlock != null ? "Block{" + registryHandler.getRegName(this.saplingBlock) + "}" : null));
	}

	@Override
	public String toReloadDataString() {
		return this.getString(Pair.of("tapering", this.tapering), Pair.of("upProbability", this.upProbability),
				Pair.of("lowestBranchHeight", this.lowestBranchHeight), Pair.of("signalEnergy", this.signalEnergy),
				Pair.of("growthRate", this.growthRate), Pair.of("soilLongevity", this.soilLongevity),
				Pair.of("soilTypeFlags", this.soilTypeFlags), Pair.of("maxBranchRadius", this.maxBranchRadius),
				Pair.of("transformable", this.transformable), Pair.of("logicKit", this.logicKit),
				Pair.of("leavesProperties", this.leavesProperties), Pair.of("envFactors", this.envFactors),
				Pair.of("dropCreatorStorage", this.dropCreatorStorage), Pair.of("megaSpecies", this.megaSpecies),
				Pair.of("seed", this.seed), Pair.of("primitive_sapling", TreeRegistry.SAPLING_REPLACERS.entrySet().stream()
						.filter(entry -> entry.getValue() == this).map(Map.Entry::getKey).findAny().orElse(CommonBlockStates.AIR)),
				Pair.of("perfectBiomes", this.perfectBiomes), Pair.of("acceptableBlocksForGrowth", this.acceptableBlocksForGrowth),
				Pair.of("genFeatures", this.genFeatures));
	}

}
