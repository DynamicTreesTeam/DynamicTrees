package com.ferreusveritas.dynamictrees.trees;

//import com.ferreusveritas.dynamictrees.DynamicTrees;
//import com.ferreusveritas.dynamictrees.DTRegistries.
//import com.ferreusveritas.dynamictrees.DTConfigs.
//import com.ferreusveritas.dynamictrees.ModConstants;
//import com.ferreusveritas.dynamictrees.api.*;
//import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
//import com.ferreusveritas.dynamictrees.api.network.MapSignal;
//import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
//import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
//import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
//import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
//import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
//import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
//import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
//import com.ferreusveritas.dynamictrees.blocks.*;
//import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
//import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
//import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
//import com.ferreusveritas.dynamictrees.event.BiomeSuitabilityEvent;
//import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
//import com.ferreusveritas.dynamictrees.growthlogic.IGrowthLogicKit;
//import com.ferreusveritas.dynamictrees.items.Seed;
//import com.ferreusveritas.dynamictrees.systems.*;
//import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorLogs;
//import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorSeed;
//import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorStorage;
//import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDisease;
//import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
//import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeInflator;
//import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeShrinker;
//import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFertilize;
//import com.ferreusveritas.dynamictrees.util.CoordUtils;
//import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
//import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
//import com.ferreusveritas.dynamictrees.worldgen.JoCode;
//import com.ferreusveritas.dynamictrees.worldgen.JoCodeStore;
//import net.minecraft.block.Block;
//import net.minecraft.block.SoundType;
//import net.minecraft.block.BlockState;
//import net.minecraft.entity.item.EntityItem;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Blocks;
//import net.minecraft.init.Items;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Direction;
//import net.minecraft.util.EnumHand;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.Vec3d;
//import net.minecraft.world.World;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.Biome;
//import net.minecraftforge.common.BiomeDictionary;
//import net.minecraftforge.common.BiomeDictionary.Type;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.common.util.Constants.NBT;
//import net.minecraftforge.event.RegistryEvent;
//import net.minecraftforge.registries.IForgeRegistry;
//import net.minecraftforge.registries.RegistryBuilder;

import com.ferreusveritas.dynamictrees.api.*;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreator;
import com.ferreusveritas.dynamictrees.api.treedata.IDropCreatorStorage;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.*;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.*;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.growthlogic.IGrowthLogicKit;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorSeed;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorStorage;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeDisease;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeInflator;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeShrinker;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.JoCodeStore;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.*;
import java.util.function.Consumer;

public class Species extends ForgeRegistryEntry<Species> {//extends net.minecraftforge.registries.IForgeRegistryEntry<Species> {

	public final static Species NULLSPECIES = new Species() {
		@Override public Seed getSeed() { return Seed.NULLSEED; }
		@Override public TreeFamily getFamily() { return TreeFamily.NULLFAMILY; }
		@Override public void addJoCodes() {}
		@Override public boolean plantSapling(IWorld world, BlockPos pos) { return false; }
		@Override public boolean generate(World world, BlockPos pos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) { return false; }
		@Override public float biomeSuitability(World world, BlockPos pos) { return 0.0f; }
		@Override public boolean addDropCreator(IDropCreator dropCreator) { return false; }
		@Override public ItemStack setSeedStack(ItemStack newSeedStack) { return new ItemStack(getSeed()); }
		@Override public ItemStack getSeedStack(int qty) { return new ItemStack(getSeed()); }
		@Override public Species setupStandardSeedDropping() { return this; }
		@Override public boolean update(World world, BlockRooty rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean rapid) { return false; }
	};

	/**
	 * Mods should use this to register their {@link Species}
	 *
	 * Places the species in a central registry.
	 * The proper place to use this is during the preInit phase of your mod.
	 */
	public static IForgeRegistry<Species> REGISTRY;

	public static void newRegistry(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<Species>()
				.setName(new ResourceLocation(DynamicTrees.MODID, "species"))
				.setDefaultKey(new ResourceLocation(DynamicTrees.MODID, "null"))
				.disableSaving()
				.setType(Species.class)
				.setIDRange(0, Integer.MAX_VALUE - 1)
				.create();
	}

	/** The family of tree this belongs to. E.g. "Oak" and "Swamp Oak" belong to the "Oak" Family*/
	protected final TreeFamily treeFamily;

	/** Logic kit for standardized extended growth behavior */
	protected IGrowthLogicKit logicKit = GrowthLogicKits.nullLogic;

	/** How quickly the branch thickens on it's own without branch merges [default = 0.3] */
	protected float tapering = 0.3f;
	/** The probability that the direction decider will choose up out of the other possible direction weights [default = 2] */
	protected int upProbability = 2;
	/** Number of blocks high we have to be before a branch is allowed to form [default = 3](Just high enough to walk under)*/
	protected int lowestBranchHeight = 3;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node [default = 16] */
	protected float signalEnergy = 16.0f;
	/** Ideal growth rate [default = 1.0]*/
	protected float growthRate = 1.0f;
	/** Ideal soil longevity [default = 8]*/
	protected int soilLongevity = 8;

	protected HashSet<Block> soilList = new HashSet<Block>();

	//Leaves
	protected ILeavesProperties leavesProperties;

	//Seeds
	/** The seed used to reproduce this species.  Drops from the tree and can plant itself */
	/** Hold damage value for seed items with multiple variants */
	protected ItemStack seedStack;
	/** A blockState that will turn itself into this tree */
	protected BlockState saplingBlock;
	/** A place to store what drops from the species. Similar to a loot table */
	protected IDropCreatorStorage dropCreatorStorage = new DropCreatorStorage();

	//WorldGen
	/** A map of environmental biome factors that change a tree's suitability */
	protected Map <BiomeDictionary.Type, Float> envFactors = new HashMap<BiomeDictionary.Type, Float>();//Environmental factors
	/** A list of JoCodes for world generation. Initialized in addJoCodes()*/
	protected JoCodeStore joCodeStore = new JoCodeStore(this);

	protected IFullGenFeature genFeatureOverride;
	protected List<IPreGenFeature> preGenFeatures;
	protected List<IPostGenFeature> postGenFeatures;
	protected List<IPostGrowFeature> postGrowFeatures;

	public int saplingModelId;

	/**
	 * Constructor only used by NULLSPECIES
	 */
	public Species() {
		this.treeFamily = TreeFamily.NULLFAMILY;
		this.leavesProperties = LeavesProperties.NULLPROPERTIES;
	}

	/**
	 * Constructor suitable for derivative mods that defaults
	 * the leavesProperties to the common type for the family
	 *
	 * @param name The simple name of the species e.g. "oak"
	 * @param treeFamily The {@link TreeFamily} that this species belongs to.
	 */
	public Species(ResourceLocation name, TreeFamily treeFamily) {
		this(name, treeFamily, treeFamily.getCommonLeaves());
	}

	/**
	 * Constructor suitable for derivative mods
	 *
	 * @param name The simple name of the species e.g. "oak"
	 * @param leavesProperties The properties of the leaves to be used for this species
	 * @param treeFamily The {@link TreeFamily} that this species belongs to.
	 */
	public Species(ResourceLocation name, TreeFamily treeFamily, ILeavesProperties leavesProperties) {
		setRegistryName(name);
		this.treeFamily = treeFamily;
//		setLeavesProperties(leavesProperties);
//
//		setStandardSoils();
//		seedStack = new ItemStack(Seed.NULLSEED);
//		saplingBlock = Blocks.AIR.getDefaultState();
//
//		//Add JoCode models for worldgen
//		addJoCodes();
//		addDropCreator(new DropCreatorLogs());
	}

	public boolean isValid() {
		return this != NULLSPECIES;
	}

	public void ifValid(Consumer<Species> c) {
		if(isValid()) {
			c.accept(this);
		}
	}

	public TreeFamily getFamily() {
		return treeFamily;
	}

	public Species setBasicGrowingParameters(float tapering, float energy, int upProbability, int lowestBranchHeight, float growthRate) {
		this.tapering = tapering;
		this.signalEnergy = energy;
		this.upProbability = upProbability;
		this.lowestBranchHeight = lowestBranchHeight;
		this.growthRate = growthRate;
		return this;
	}

	public float getEnergy(World world, BlockPos rootPos) {
		return getGrowthLogicKit().getEnergy(world, rootPos, this, signalEnergy);
	}

	public float getGrowthRate(World world, BlockPos rootPos) {
		return growthRate;
	}

	/** Probability reinforcer for up direction which is arguably the direction most trees generally grow in.*/
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
		return getLowestBranchHeight();
	}

	public float getTapering() {
		return tapering;
	}

	///////////////////////////////////////////
	//LEAVES
	///////////////////////////////////////////

	public Species setLeavesProperties(ILeavesProperties leavesProperties) {
		this.leavesProperties = leavesProperties;
		return this;
	}

	public ILeavesProperties getLeavesProperties() {
		return leavesProperties;
	}

	///////////////////////////////////////////
	//SEEDS
	///////////////////////////////////////////

	/**
	 * Get a copy of the {@link Seed} stack with the supplied quantity.
	 * This is necessary because the stack may be combined with
	 * NBT data.
	 *
	 * @param qty The number of items in the newly copied stack.
	 * @return A copy of the {@link ItemStack} with the {@link Seed} inside.
	 */
	public ItemStack getSeedStack(int qty) {
		ItemStack stack = seedStack.copy();
		stack.setCount(MathHelper.clamp(qty, 0, 64));
		return stack;
	}

	public Seed getSeed() {
		return (Seed) seedStack.getItem();
	}

	/**
	 * Generate a seed. Developer is still required to register the item
	 * in the appropriate registries.
	 */
	public Species generateSeed() {
		Seed seed = new Seed(getRegistryName().getPath() + "seed");
		setSeedStack(new ItemStack(seed));
		return this;
	}

	/**
	 * Sets the {@link ItemStack} that is used for this Species.
	 * The {@link ItemStack} must contain an {@link Item} of type {@link Seed}
	 * or this will fail.
	 *
	 * This links the {@link Seed} to the {@link Species} and vice versa.
	 *
	 * @param newSeedStack The input {@link ItemStack} containing a {@link Seed} item.
	 * @return The input {@link ItemStack} or an {@link ItemStack#EMPTY} on failure.
	 */
	public ItemStack setSeedStack(ItemStack newSeedStack) {

		if(newSeedStack.getItem() instanceof Seed) {
			seedStack = newSeedStack;
			Seed seed = (Seed) seedStack.getItem();
			seed.setSpecies(this, seedStack);
			return seedStack;
		} else {
			System.err.println("setSeedStack must have an ItemStack with an Item that is an instance of a Seed");
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Sets up a standardized drop system for
	 * Harvest, Voluntary, and Leaves Drops.
	 *
	 * Typically called in the constructor
	 */
	public Species setupStandardSeedDropping() {
		addDropCreator(new DropCreatorSeed());
		return this;
	}

	public boolean addDropCreator(IDropCreator dropCreator) {
		return dropCreatorStorage.addDropCreator(dropCreator);
	}

	public boolean remDropCreator(ResourceLocation dropCreatorName) {
		return dropCreatorStorage.remDropCreator(dropCreatorName);
	}

	public Map<ResourceLocation, IDropCreator> getDropCreators() {
		return dropCreatorStorage.getDropCreators();
	}

	/**
	 * Gets a list of drops for a {@link BlockDynamicLeaves} when the entire tree is harvested.
	 * NOT used for individual {@link BlockDynamicLeaves} being directly harvested by hand or tool.
	 *
	 * @param world
	 * @param leafPos
	 * @param dropList
	 * @param random
	 * @return
	 */
	public List<ItemStack> getTreeHarvestDrops(World world, BlockPos leafPos, List<ItemStack> dropList, Random random) {
		dropList = TreeRegistry.globalDropCreatorStorage.getHarvestDrop(world, this, leafPos, random, dropList, 0, 0);
		return dropCreatorStorage.getHarvestDrop(world, this, leafPos, random, dropList, 0, 0);
	}

	/**
	 * Gets a {@link List} of voluntary drops.  Voluntary drops are {@link ItemStack}s that fall from the {@link TreeFamily} at
	 * random with no player interaction.
	 *
	 * @param world
	 * @param rootPos
	 * @param treePos
	 * @param soilLife
	 * @return
	 */
	public List<ItemStack> getVoluntaryDrops(World world, BlockPos rootPos, BlockPos treePos, int soilLife) {
		List<ItemStack> dropList = TreeRegistry.globalDropCreatorStorage.getVoluntaryDrop(world, this, rootPos, world.rand, null, soilLife);
		return dropCreatorStorage.getVoluntaryDrop(world, this, rootPos, world.rand, dropList, soilLife);
	}

	/**
	 * Gets a {@link List} of Leaves drops.  Leaves drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BlockDynamicLeaves} directly by hand or with a tool.
	 *
	 * @param access
	 * @param breakPos
	 * @param dropList
	 * @param fortune
	 * @return
	 */
	public List<ItemStack> getLeavesDrops(World access, BlockPos breakPos, List<ItemStack> dropList, int fortune) {
		Random random = access instanceof World ? ((World)access).rand : new Random();
		dropList = TreeRegistry.globalDropCreatorStorage.getLeavesDrop(access, this, breakPos, random, dropList, fortune);
		return dropCreatorStorage.getLeavesDrop(access, this, breakPos, random, dropList, fortune);
	}


	/**
	 * Gets a {@link List} of Logs drops.  Logs drops are {@link ItemStack}s that result from the breaking of
	 * a {@link BlockBranch} directly by hand or with a tool.
	 *
	 * @param world
	 * @param breakPos
	 * @param dropList
	 * @param volume
	 * @return
	 */
	public List<ItemStack> getLogsDrops(World world, BlockPos breakPos, List<ItemStack> dropList, float volume) {
		dropList = TreeRegistry.globalDropCreatorStorage.getLogsDrop(world, this, breakPos, world.rand, dropList, volume);
		return dropCreatorStorage.getLogsDrop(world, this, breakPos, world.rand, dropList, volume);
	}

	public class LogsAndSticks {
		public final int logs;
		public final int sticks;
		public LogsAndSticks(int logs, int sticks) { this.logs = logs; this.sticks = DTConfigs.dropSticks.get() ? sticks : 0; };
	}

	public LogsAndSticks getLogsAndSticks(float volume) {
		int logs = (int) volume; // Drop vanilla logs or whatever
		int sticks = (int) ((volume - logs) * 8);// A stick is 1/8th of a log (1 log = 4 planks, 2 planks = 4 sticks) Give him the stick!
		return new LogsAndSticks(logs, sticks);
	}

	/**
	 *
	 * @param world
	 * @param endPoints
	 * @param rootPos
	 * @param treePos
	 * @param soilLife
	 * @return true if seed was dropped
	 */
	public boolean handleVoluntaryDrops(World world, List<BlockPos> endPoints, BlockPos rootPos, BlockPos treePos, int soilLife) {
		int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
		if(tickSpeed > 0) {
			double slowFactor = 3.0 / tickSpeed;//This is an attempt to normalize voluntary drop rates.
			if(world.rand.nextDouble() < slowFactor) {
				List<ItemStack> drops = getVoluntaryDrops(world, rootPos, treePos, soilLife);

				if(!drops.isEmpty() && !endPoints.isEmpty()) {
					for(ItemStack drop: drops) {
						BlockPos branchPos = endPoints.get(world.rand.nextInt(endPoints.size()));
						branchPos = branchPos.up();//We'll aim at the block above the end branch. Helps with Acacia leaf block formations
						BlockPos itemPos = CoordUtils.getRayTraceFruitPos(world, this, treePos, branchPos, SafeChunkBounds.ANY);

//						if(itemPos != BlockPos.ZERO) {
//							ItemEntity itemEntity = new ItemEntity(world, itemPos.getX() + 0.5, itemPos.getY() + 0.5, itemPos.getZ() + 0.5, drop);
//							Vec3d motion = new Vec3d(itemPos).subtract(new Vec3d(treePos));
//							float distAngle = 15;//The spread angle(center to edge)
//							float launchSpeed = 4;//Blocks(meters) per second
//							motion = new Vec3d(motion.x, 0, motion.y).normalize().rotateYaw((world.rand.nextFloat() * distAngle * 2) - distAngle).scale(launchSpeed/20f);
//							itemEntity.motionX = motion.x;
//							itemEntity.motionY = motion.y;
//							itemEntity.motionZ = motion.z;
//							return world.spawnEntity(itemEntity);
//						}
					}
				}
			}
		}
		return true;
	}
//
//	///////////////////////////////////////////
//	//SAPLING
//	///////////////////////////////////////////

	/**
	 * Checks surroundings and places a dynamic sapling block.
	 *
	 * @param world
	 * @param pos
	 * @return true if the planting was successful
	 */
	public boolean plantSapling(IWorld world, BlockPos pos) {

		if(world.getBlockState(pos).getBlock().canBeReplacedByLeaves(world.getBlockState(pos), world, pos) && BlockDynamicSapling.canSaplingStay(world, this, pos)) {
			DTRegistries.blockDynamicSapling.setSpecies(world, pos, this);
			return true;
		}

		return false;
	}

	//This is for the sapling.
	//If false is returned then nothing happens.
	//If true is returned canUseBoneMealNow is run then the bonemeal is consumed regardless of it's return.
	public boolean canGrowWithBoneMeal(World world, BlockPos pos) {
		return canBoneMeal();
	}

	//This is for the sapling.
	//Return weather or not the bonemealing should cause growth
	public boolean canUseBoneMealNow(World world, Random rand, BlockPos pos) {
		return canBoneMeal();
	}

	//This is for the tree itself.
	public boolean canBoneMeal() {
		return true;
	}

	public boolean transitionToTree(World world, BlockPos pos) {
		//Ensure planting conditions are right
		TreeFamily family = getFamily();
		if(world.isAirBlock(pos.up()) && isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
			family.getDynamicBranch().setRadius(world, pos, (int)family.getPrimaryThickness(), null);//set to a single branch with 1 radius
			world.setBlockState(pos.up(), getLeavesProperties().getDynamicLeavesState());//Place a single leaf block on top
			placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty dirt underneath
			return true;
		}

		return false;
	}

	public AxisAlignedBB getSaplingBoundingBox() {
		return new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
	}

	//This is used to load the sapling model
	public ResourceLocation getSaplingName() {
		return getRegistryName();
	}

	public int saplingColorMultiplier(BlockState state, IEnviromentBlockReader access, BlockPos pos, int tintIndex) {
		return getLeavesProperties().foliageColorMultiplier(state, access, pos);
	}

	public SoundType getSaplingSound() {
		return SoundType.PLANT;
	}

	///////////////////////////////////////////
	//DIRT
	///////////////////////////////////////////

	public BlockRooty getRootyBlock() {
		return DTRegistries.blockRootyDirt;
	}

	public boolean placeRootyDirtBlock(World world, BlockPos rootPos, int life) {
		world.setBlockState(rootPos, getRootyBlock().getDefaultState().with(BlockRooty.LIFE, life));
		return true;
	}

	public Species setSoilLongevity(int longevity) {
		soilLongevity = longevity;
		return this;
	}

	public int getSoilLongevity(World world, BlockPos rootPos) {
		return (int)(biomeSuitability(world, rootPos) * soilLongevity);
	}

	public boolean isThick() {
		return false;
	}

	public int maxBranchRadius() {
		return isThick() ? BlockBranchThick.RADMAX_THICK : BlockBranch.RADMAX_NORMAL;
	}

	/**
	 * Adds blocks to the acceptable soil list.
	 *
	 * @param soilBlocks
	 */
	public Species addAcceptableSoil(Block... soilBlocks) {
		Collections.addAll(soilList, soilBlocks);
		return this;
	}

	/**
	 * Removes blocks from the acceptable soil list.
	 *
	 * @param soilBlocks
	 */
	public Species remAcceptableSoil(Block... soilBlocks) {
		for(Block block : soilBlocks) {
			soilList.remove(block);
		}
		return this;
	}

	/**
	 * Will clear the acceptable soils list.  Useful for
	 * making trees that can only be planted in abnormal
	 * substrates.
	 */
	public Species clearAcceptableSoils() {
		soilList.clear();
		return this;
	}

	/**
	 * Retrieve a clone of the acceptable soils list.
	 * Editing this set will not affect the original list.
	 * Should only be used for config purposes and is not
	 * recommended for realtime gameplay operations.
	 *
	 * @return A clone of the acceptable soils list.
	 */
	public Set<Block> getAcceptableSoils() {
		return (Set<Block>) soilList.clone();
	}

	/**
	 * This is run by the Species class itself to set the standard
	 * blocks available to be used as planting substrate. Developer
	 * may override this entirely or just modify the list at a
	 * later time.
	 */
	protected final void setStandardSoils() {
		addAcceptableSoil(Blocks.DIRT, Blocks.GRASS, Blocks.MYCELIUM);
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
	public boolean isAcceptableSoil(IWorld world, BlockPos pos, BlockState soilBlockState) {
		Block soilBlock = soilBlockState.getBlock();
		return soilList.contains(soilBlock) || soilBlock instanceof BlockRooty;
	}

	/**
	 * Version of soil acceptability tester that is only run for worldgen.  This allows for Swamp oaks and stuff.
	 *
	 * @param world
	 * @param pos
	 * @param soilBlockState
	 * @return
	 */
	public boolean isAcceptableSoilForWorldgen(World world, BlockPos pos, BlockState soilBlockState) {
		return isAcceptableSoil(world, pos, soilBlockState);
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
	 * @param rootyDirt The {@link BlockRooty} that is supporting this tree
	 * @param rootPos The {@link BlockPos} of the {@link BlockRooty} type in the world
	 * @param soilLife The life of the soil. 0: Depleted -> 15: Full
	 * @param treePos The {@link BlockPos} of the {@link TreeFamily} trunk base.
	 * @param random A random number generator
	 * @param natural Set this to true if this member is being used to naturally grow the tree(create drops or fruit)
	 * @return true if network is viable.  false if network is not viable(will destroy the {@link BlockRooty} this tree is on)
	 */
	public boolean update(World world, BlockRooty rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean natural) {

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
	 * @param treePos The {@link BlockPos} of the base of the {@link TreeFamily} trunk
	 * @param treeBase The tree part that is the base of the {@link TreeFamily} trunk.  Provided for easy analysis.
	 * @return A list of all branch endpoints for the {@link TreeFamily}
	 */
	final protected List<BlockPos> getEnds(World world, BlockPos treePos, ITreePart treeBase) {
		NodeFindEnds endFinder = new NodeFindEnds();
		treeBase.analyse(world.getBlockState(treePos), world, treePos, null, new MapSignal(endFinder));
		return endFinder.getEnds();
	}

	/**
	 * A rot handler.
	 *
	 * @param world The world
	 * @param ends A {@link List} of {@link BlockPos}s of {@link BlockBranch} endpoints.
	 * @param rootPos The {@link BlockPos} of the {@link BlockRooty} for this {@link TreeFamily}
	 * @param treePos The {@link BlockPos} of the trunk base for this {@link TreeFamily}
	 * @param soilLife The soil life of the {@link BlockRooty}
	 * @param safeBounds The defined boundaries where it is safe to make block changes
	 * @return true if last piece of tree rotted away.
	 */
	public boolean handleRot(World world, List<BlockPos> ends, BlockPos rootPos, BlockPos treePos, int soilLife, SafeChunkBounds safeBounds) {

		Iterator<BlockPos> iter = ends.iterator();//We need an iterator since we may be removing elements.
		SimpleVoxmap leafMap = getLeavesProperties().getCellKit().getLeafCluster();

		while (iter.hasNext()) {
			BlockPos endPos = iter.next();
			BlockState branchState = world.getBlockState(endPos);
			BlockBranch branch = TreeHelper.getBranch(branchState);
			if(branch != null) {
				int radius = branch.getRadius(branchState);
				float rotChance = rotChance(world, endPos, world.rand, radius);
				if(branch.checkForRot(world, endPos, this, radius, world.rand, rotChance, safeBounds != SafeChunkBounds.ANY) || radius != 1) {
					if(safeBounds != SafeChunkBounds.ANY) { //worldgen
						TreeHelper.ageVolume(world, endPos.down((leafMap.getLenZ() - 1) / 2), (leafMap.getLenX() - 1) / 2, leafMap.getLenY(), 2, safeBounds);
					}
					iter.remove();//Prune out the rotted end points so we don't spawn fruit from them.
				}
			}
		}

		return ends.isEmpty() && !TreeHelper.isBranch(world.getBlockState(treePos));//There are no endpoints and the trunk is missing
	}

	static private final Direction upFirst[] = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

	/**
	* Handle rotting branches
	* @param world The world
	* @param pos
	* @param neighborCount Count of neighbors reinforcing this block
	* @param radius The radius of the branch
	* @param random Access to a random number generator
	* @param rapid True if this rot is happening under a generation scenario as opposed to natural tree updates
	* @return true if the branch should rot
	*/
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {

		if(radius <= 1) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) getLeavesProperties().getDynamicLeavesState().getBlock();
			for(Direction dir: upFirst) {
				if(leaves.growLeavesIfLocationIsSuitable(world, getLeavesProperties(), pos.offset(dir), 0)) {
					return false;
				}
			}
		}

		if(rapid || (DTConfigs.maxBranchRotRadius.get() != 0 && radius <= DTConfigs.maxBranchRotRadius.get())) {
			BlockBranch branch = TreeHelper.getBranch(world.getBlockState(pos));
			if(branch != null) {
				branch.rot(world, pos);
			}
			return true;
		}

		return false;
	}

	/**
	 * Provides the chance that a log will rot.
	 *
	 * @param world The world
	 * @param pos The {@link BlockPos} of the {@link BlockBranch}
	 * @param rand A random number generator
	 * @param radius The radius of the {@link BlockBranch}
	 * @return The chance this will rot. 0.0(never) -> 1.0(always)
	 */
	public float rotChance(World world, BlockPos pos, Random rand, int radius) {
		return 0.3f + ((8 - radius) * 0.1f);// Thicker branches take longer to rot
	}

	/**
	 * The grow handler.
	 *
	 * @param world The world
	 * @param rootyDirt The {@link BlockRooty} that is supporting this tree
	 * @param rootPos The {@link BlockPos} of the {@link BlockRooty} type in the world
	 * @param soilLife The life of the soil. 0: Depleted -> 15: Full
	 * @param treePos The {@link BlockPos} of the {@link TreeFamily} trunk base.
	 * @param random A random number generator
	 * @param natural
	 * 		If true then this member is being used to grow the tree naturally(create drops or fruit).
	 *      If false then this member is being used to grow a tree with a growth accelerant like bonemeal or the potion of burgeoning
	 * @return true if network is viable.  false if network is not viable(will destroy the {@link BlockRooty} this tree is on)
	 */
	public boolean grow(World world, BlockRooty rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean natural) {

		float growthRate = (float) (getGrowthRate(world, rootPos) * DTConfigs.treeGrowthMultiplier.get() * DTConfigs.treeGrowthFolding.get());
		do {
			if(soilLife > 0){
				if(growthRate > random.nextFloat()) {
					GrowSignal signal = new GrowSignal(this, rootPos, getEnergy(world, rootPos));
					boolean success = treeBase.growSignal(world, treePos, signal).success;

					int soilLongevity = getSoilLongevity(world, rootPos) * (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed

					if(soilLongevity <= 0 || random.nextInt(soilLongevity) == 0) {//1 in X(soilLongevity) chance to draw nutrients from soil
						rootyDirt.setSoilLife(world, rootPos, soilLife - 1);//decrement soil life
					}

					if(signal.choked) {
						soilLife = 0;
						rootyDirt.setSoilLife(world, rootPos, soilLife);
						TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new NodeShrinker(signal.getSpecies())));
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
	public Species setGrowthLogicKit(IGrowthLogicKit logicKit) {
		this.logicKit = logicKit;
		return this;
	}

	public IGrowthLogicKit getGrowthLogicKit() {
		return logicKit;
	}

	/**
	* Selects a new direction for the branch(grow) signal to turn to.
	* This function uses a probability map to make the decision and is acted upon by the GrowSignal() function in the branch block.
	* Can be overridden for different species but it's preferable to override customDirectionManipulation.
	*
	* @param world The World
	* @param pos
	* @param branch The branch block the GrowSignal is traveling in.
	* @param signal The grow signal.
	* @return
	*/
	public Direction selectNewDirection(World world, BlockPos pos, BlockBranch branch, GrowSignal signal) {
		Direction originDir = signal.dir.getOpposite();

		//prevent branches on the ground
		if(signal.numSteps + 1 <= getLowestBranchHeight(world, signal.rootPos)) {
			return Direction.UP;
		}

		int probMap[] = new int[6];//6 directions possible DUNSWE

		//Probability taking direction into account
		probMap[Direction.UP.ordinal()] = signal.dir != Direction.DOWN ? getUpProbability(): 0;//Favor up
		probMap[signal.dir.ordinal()] += getReinfTravel(); //Favor current direction

		//Create probability map for direction change
		for(Direction dir: Direction.values()) {
			if(!dir.equals(originDir)) {
				BlockPos deltaPos = pos.offset(dir);
				//Check probability for surrounding blocks
				//Typically Air:1, Leaves:2, Branches: 2+r
				BlockState deltaBlockState = world.getBlockState(deltaPos);
				probMap[dir.getIndex()] += TreeHelper.getTreePart(deltaBlockState).probabilityForBlock(deltaBlockState, world, deltaPos, branch);
			}
		}

		//Do custom stuff or override probability map for various species
		probMap = customDirectionManipulation(world, pos, branch.getRadius(world.getBlockState(pos)), signal, probMap);

		//Select a direction from the probability map
		int choice = com.ferreusveritas.dynamictrees.util.MathHelper.selectRandomFromDistribution(signal.rand, probMap);//Select a direction from the probability map
		return newDirectionSelected(Direction.values()[choice != -1 ? choice : 1], signal);//Default to up if things are screwy
	}

	/** Species can override the probability map here **/
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
		return getGrowthLogicKit().directionManipulation(world, pos, this, radius, signal, probMap);
	}

	/** Species can override to take action once a new direction is selected **/
	protected Direction newDirectionSelected(Direction newDir, GrowSignal signal) {
		return getGrowthLogicKit().newDirectionSelected(this, newDir, signal);
	}

	/**
	 * Allows a species to do things after a grow event just occured.  Such as used
	 * by Jungle trees to create cocoa pods on the trunk
	 *
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block
	 * @param treePos The position of the base trunk block of the tree(usually directly above the rooty dirt block)
	 * @param soilLife The life of the soil block this tree is planted in
	 * @param natural
	 * 		If true then this member is being used to grow the tree naturally(create drops or fruit).
	 * 		If false then this member is being used to grow a tree with a growth accelerant like bonemeal or the potion of burgeoning
	 */
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean natural) {
		if(postGrowFeatures != null) {
			for(IPostGrowFeature feature: postGrowFeatures) {
				feature.postGrow(world, rootPos, treePos, this, soilLife, natural);
			}
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
		if(soilLife == 0 && DTConfigs.diseaseChance.get() > random.nextFloat() ) {
			baseTreePart.analyse(world.getBlockState(treePos), world, treePos, Direction.DOWN, new MapSignal(new NodeDisease(this)));
			return true;
		}

		return false;
	}


	//////////////////////////////
	// BIOME HANDLING
	//////////////////////////////

//	public Species envFactor(Type type, float factor) {
//		envFactors.put(type, factor);
//		return this;
//	}

	/**
	*
	* @param world The World
	* @param pos
	* @return range from 0.0 - 1.0.  (0.0f for completely unsuited.. 1.0f for perfectly suited)
	*/
	public float biomeSuitability(World world, BlockPos pos) {

		Biome biome = world.getBiome(pos);

//		//An override to allow other mods to change the behavior of the suitability for a world location. Such as Terrafirmacraft.
//		BiomeSuitabilityEvent suitabilityEvent = new BiomeSuitabilityEvent(world, biome, this, pos);
//		MinecraftForge.EVENT_BUS.post(suitabilityEvent);
//		if(suitabilityEvent.isHandled()) {
//			return suitabilityEvent.getSuitability();
//		}
//
		float ugs = (float)(double) DTConfigs.scaleBiomeGrowthRate.get();//universal growth scalar
//
//		if(ugs == 1.0f || isBiomePerfect(biome)) {
//			return 1.0f;
//		}
//
		float suit = defaultSuitability();
//
//		for(Type t : BiomeDictionary.getTypes(biome)) {
//			suit *= envFactors.containsKey(t) ? envFactors.get(t) : 1.0f;
//		}

		//Linear interpolation of suitability with universal growth scalar
		suit = ugs <= 0.5f ? ugs * 2.0f * suit : ((1.0f - ugs) * suit + (ugs - 0.5f)) * 2.0f;

		return (float)MathHelper.clamp(suit, 0.0f, 1.0f);
	}

	/**
	 * Used to determine if the provided {@link Biome} argument will yield
	 * unhindered growth to Maximum potential. This has the affect of the
	 * suitability being 100%(or 1.0f)
	 *
	 * @param biome The biome being tested
	 * @return True if biome is "perfect" false otherwise.
	 */
	public boolean isBiomePerfect(Biome biome) {
		return false;
	}

	/** A value that determines what a tree's suitability is before climate manipulation occurs. */
	public static final float defaultSuitability() {
		return 0.85f;
	}
//
//	/**
//	* A convenience function to test if a biome is one of the many options passed.
//	*
//	* @param biomeToCheck The biome we are matching
//	* @param biomes Multiple biomes to match against
//	* @return True if a match is found. False if not.
//	*/
//	public static boolean isOneOfBiomes(Biome biomeToCheck, Biome... biomes) {
//		for(Biome biome: biomes) {
//			if(biomeToCheck == biome) {
//				return true;
//			}
//		}
//		return false;
//	}

	//////////////////////////////
	// INTERACTIVE
	//////////////////////////////

	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {

		//Bonemeal fertilizes the soil and causes a single growth pulse
		if( canBoneMeal() && itemStack.getItem() == Items.BONE_MEAL) {
			return new SubstanceFertilize().setAmount(1).setGrow(true);
		}

		//Use substance provider interface if it's available
		if(itemStack.getItem() instanceof ISubstanceEffectProvider) {
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

		ISubstanceEffect effect = getSubstanceEffect(itemStack);

		if(effect != null) {
			if(effect.isLingering()) {
//				world.spawnEntity(new EntityLingeringEffector(world, rootPos, effect));
//				return true;
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
	 * @param rootPos The  {@link BlockPos} of the {@link BlockRooty}
	 * @param hitPos The {@link BlockPos} of the {@link Block} that was hit.
	 * @param state The {@link BlockState} of the hit {@link Block}.
	 * @param player The {@link PlayerEntity} that hit the {@link Block}
	 * @param hand Hand used to peform the action
	 * @param heldItem The {@link ItemStack} the {@link PlayerEntity} hit the {@link Block} with.
	 * @param side The side of the block that was hit.
	 * @param hitX X axis of hit with hitPos
	 * @param hitY Y axis of hit with hitPos
	 * @param hitZ Z axis of hit with hitPos
	 * @return True if action was handled, false otherwise.
	 */
	public boolean onTreeActivated(World world, BlockPos rootPos, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, Direction side, float hitX, float hitY, float hitZ) {

		if (heldItem != null) {//Something in the hand
			if(applySubstance(world, rootPos, hitPos, player, hand, heldItem)) {
				Species.consumePlayerItem(player, hand, heldItem);
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
		if(!player.isCreative()) {
			if (heldItem.getItem() instanceof IEmptiable) {//A substance deployed from a refillable container
				IEmptiable emptiable = (IEmptiable) heldItem.getItem();
				player.setHeldItem(hand, emptiable.getEmptyContainer());
			}
			else if(heldItem.getItem() == Items.POTION) {//An actual potion
				player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
			} else {
				heldItem.shrink(1);//Just a regular item like bonemeal
			}
		}
	}

	///////////////////////////////////////////
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////

	public IAnimationHandler selectAnimationHandler(EntityFallingTree fallingEntity) {
		return getFamily().selectAnimationHandler(fallingEntity);
	}

	//////////////////////////////
	// BONSAI POT
	//////////////////////////////

	/**
	 * Provides the {@link BlockBonsaiPot} for this Species.  A mod can
	 * derive it's own BonzaiPot subclass if it wants something custom.
	 *
	 * @return A {@link BlockBonsaiPot}
	 */
	public BlockBonsaiPot getBonzaiPot() {
		return DTRegistries.blockBonsaiPot;
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
	 * @param rootPos The position of {@link BlockRooty} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param radius The radius of the tree generation boundary
	 * @return true if tree was generated. false otherwise.
	 */
	public boolean generate(World world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {

		if(genFeatureOverride != null) {
			return genFeatureOverride.generate(world, rootPos, this, biome, random, radius, safeBounds);
		}

		Direction facing = CoordUtils.getRandomDir(random);
		if(getJoCodeStore() != null) {
			JoCode code = getJoCodeStore().getRandomCode(radius, random);
			if(code != null) {
				code.generate(world, this, rootPos, biome, facing, radius, safeBounds);
				return true;
			}
		}

		return false;
	}

	public JoCodeStore getJoCodeStore() {
		return joCodeStore;
	}

	public JoCode getJoCode(String joCodeString) {
		return new JoCode(joCodeString);
	}

	/**
	 * A {@link JoCode} defines the block model of the {@link TreeFamily}
	 */
	public void addJoCodes() {
		joCodeStore.addCodesFromFile(this, "assets/" + getRegistryName().getNamespace() + "/trees/"+ getRegistryName().getPath() + ".txt");
	}

	public Species addGenFeature(IGenFeature module) {
		addGenFeature(module, IGenFeature.DEFAULTS);
		return this;
	}

	public Species addGenFeature(IGenFeature module, int allowableFlags) {

		if(module instanceof IFullGenFeature && (allowableFlags & IGenFeature.FULLGEN) != 0) {
			genFeatureOverride = (IFullGenFeature) module;
		}

		if(module instanceof IPreGenFeature && (allowableFlags & IGenFeature.PREGEN) != 0) {
			IPreGenFeature feature = (IPreGenFeature) module;
			if(preGenFeatures == null) {
				preGenFeatures = new ArrayList<>(1);
			}
			preGenFeatures.add(feature);
		}

		if(module instanceof IPostGenFeature && (allowableFlags & IGenFeature.POSTGEN) != 0) {
			IPostGenFeature feature = (IPostGenFeature) module;
			if(postGenFeatures == null) {
				postGenFeatures = new ArrayList<>(1);
			}
			postGenFeatures.add(feature);
		}

		if(module instanceof IPostGrowFeature && (allowableFlags & IGenFeature.POSTGROW) != 0) {
			IPostGrowFeature feature = (IPostGrowFeature) module;
			if(postGrowFeatures == null) {
				postGrowFeatures = new ArrayList<>(1);
			}
			postGrowFeatures.add(feature);
		}

		return this;
	}

	/**
	 * Allows the tree to prepare the area for planting.  For thick tree this may include removing blocks around the trunk that
	 * could be in the way.
	 *
	 * @param world The world
	 * @param rootPos The position of {@link BlockRooty} this tree will be planted in
	 * @param radius The radius of the generation area
	 * @param facing The direction the joCode will build the tree
	 * @param safeBounds An object that helps prevent accessing blocks in unloaded chunks
	 * @param joCode The joCode that will be used to grow the tree
	 * @return new blockposition of root block.  BlockPos.ZERO to cancel generation
	 */
	public BlockPos preGeneration(World world, BlockPos rootPos, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode) {
		if(preGenFeatures != null) {
			for(IPreGenFeature feature: preGenFeatures) {
				rootPos = feature.preGeneration(world, rootPos, this, radius, facing, safeBounds, joCode);
			}
		}
		return rootPos;
	}

	/**
	 * Allows the tree to decorate itself after it has been generated.
	 * Use this to add vines, add fruit, fix the soil, add butress roots etc.
	 *
	 * @param world The world
	 * @param rootPos The position of {@link BlockRooty} this tree is planted in
	 * @param biome The biome this tree is generating in
	 * @param radius The radius of the tree generation boundary
	 * @param endPoints A {@link List} of {@link BlockPos} in the world designating branch endpoints
	 * @param safeBounds An object that helps prevent accessing blocks in unloaded chunks
  	 * @param initialDirtState The blockstate of the dirt that became rooty.  Useful for matching terrain.
	 */
	public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		if(postGenFeatures != null) {
			for(IPostGenFeature feature: postGenFeatures) {
				feature.postGeneration(world, rootPos, this, biome, radius, endPoints, safeBounds, initialDirtState);
			}
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
		return new NodeInflator(this, leafMap);
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
	public String toString() {
		if (getRegistryName() == null){
			return null;
		}
		return getRegistryName().toString();
	}

}
