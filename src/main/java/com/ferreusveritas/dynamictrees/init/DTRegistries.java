package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.DynamicCocoaBlock;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyWaterBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SpreadableRootyBlock;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.systems.dropcreators.FruitDropCreator;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance;
import com.ferreusveritas.dynamictrees.tileentity.PottedSaplingTileEntity;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.MushroomFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.TreeFeatureCanceller;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HugeFungusConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {

	/**
	 * This is the creative tab that holds all DT items. Must be instantiated here
	 * so that it's not {@code null} when we create blocks and items.
	 */
	public static final ItemGroup ITEM_GROUP = new ItemGroup(DynamicTrees.MOD_ID) {
		@Override
		public ItemStack makeIcon() {
			return TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1);
		}
	};

	///////////////////////////////////////////
	// BLOCKS
	///////////////////////////////////////////

	/** An apple fruit block. */
	public static final FruitBlock APPLE_FRUIT = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE))
			.setCanBoneMeal(DTConfigs.CAN_BONE_MEAL_APPLE::get);

	/** A modified cocoa fruit block (for dynamic trees). */
	public static final DynamicCocoaBlock COCOA_FRUIT = new DynamicCocoaBlock();

	/** A potted sapling block, which is a normal pot but for dynamic saplings. */
	public static final PottedSaplingBlock POTTED_SAPLING = new PottedSaplingBlock();

	/** A trunk shell block, which is the outer block for thick branches. */
	public static final TrunkShellBlock TRUNK_SHELL = new TrunkShellBlock();
	
	public static void setup() {
		setupBlocks();
		setUpSoils();
		setupConnectables();
		setupItems();
	}

	public static void setupBlocks() {
		RegistryHandler.addBlock(DynamicTrees.resLoc("apple_fruit"), APPLE_FRUIT);
		RegistryHandler.addBlock(DynamicTrees.resLoc("cocoa"), COCOA_FRUIT);
		RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
		RegistryHandler.addBlock(DynamicTrees.resLoc("trunk_shell"), TRUNK_SHELL);
	}

	private static void setUpSoils() {
		DirtHelper.registerSoil(Blocks.GRASS_BLOCK, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.DIRT, DirtHelper.DIRT_LIKE, new SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM));
		DirtHelper.registerSoil(Blocks.COARSE_DIRT, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.PODZOL, DirtHelper.DIRT_LIKE);
		DirtHelper.registerSoil(Blocks.FARMLAND, DirtHelper.DIRT_LIKE, Blocks.DIRT);
		DirtHelper.registerSoil(Blocks.SAND, DirtHelper.SAND_LIKE);
		DirtHelper.registerSoil(Blocks.RED_SAND, DirtHelper.SAND_LIKE);
		DirtHelper.registerSoil(Blocks.GRAVEL, DirtHelper.GRAVEL_LIKE);
		DirtHelper.registerSoil(Blocks.WATER, DirtHelper.WATER_LIKE, new RootyWaterBlock(Blocks.WATER));
		DirtHelper.registerSoil(Blocks.MYCELIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.FUNGUS_LIKE);
		DirtHelper.registerSoil(Blocks.NETHERRACK, DirtHelper.NETHER_LIKE, new SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM));
		DirtHelper.registerSoil(Blocks.SOUL_SAND, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHER_LIKE);
		DirtHelper.registerSoil(Blocks.SOUL_SOIL, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.CRIMSON_NYLIUM, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.WARPED_NYLIUM, DirtHelper.NETHER_SOIL_LIKE);
		DirtHelper.registerSoil(Blocks.END_STONE, DirtHelper.END_LIKE);
	}

	public static void setupConnectables() {
		BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state,world,pos,side)->{
			if (side == Direction.DOWN) return 1;
			return 0;
		});

		BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state,world,pos,side)->{
			if (side == Direction.DOWN){
				BlockState branchState = world.getBlockState(pos.relative(Direction.UP));
				BranchBlock branch = TreeHelper.getBranch(branchState);
				if (branch != null)
					return MathHelper.clamp(branch.getRadius(branchState) - 1, 1, 8);
				else
					return 8;
			}
			return 0;
		});
	}

	@SubscribeEvent
	public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
		final Species appleOak = Species.REGISTRY.get(DynamicTrees.resLoc("apple_oak"));

		if (appleOak.isValid()){
			appleOak.addDropCreator(new FruitDropCreator());
			APPLE_FRUIT.setSpecies(appleOak);
		}

		for (RootyBlock rooty : RootyBlockHelper.generateListForRegistry(false, DynamicTrees.MOD_ID))
			event.getRegistry().register(rooty);
	}
	
	///////////////////////////////////////////
	// ITEMS
	///////////////////////////////////////////

	/** A custom potion called the Dendro Potion, houses all tree potions. */
	public static final DendroPotion DENDRO_POTION = new DendroPotion();

	/** A bucket of dirt item, for crafting saplings into seeds and vice versa. */
	public static final DirtBucket DIRT_BUCKET = new DirtBucket();

	/** A staff, a creative tool for copying and pasting tree shapes. */
	public static final Staff STAFF = new Staff();

	public static void setupItems() {
		RegistryHandler.addItem(DynamicTrees.resLoc("staff"), STAFF);
		RegistryHandler.addItem(DynamicTrees.resLoc("dirt_bucket"), DIRT_BUCKET);
		RegistryHandler.addItem(DynamicTrees.resLoc("dendro_potion"), DENDRO_POTION);
	}

	///////////////////////////////////////////
	// ENTITIES
	///////////////////////////////////////////
	
	public final static String FALLING_TREE_ID = "falling_tree";
	public final static String LINGERING_EFFECTOR_ID = "lingering_effector";
	
	public static final EntityType<FallingTreeEntity> FALLING_TREE = EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, EntityClassification.MISC)
			.setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(512)
			.setUpdateInterval(Integer.MAX_VALUE)
			.setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(world))
			.build(FALLING_TREE_ID);
	public static final EntityType<LingeringEffectorEntity> LINGERING_EFFECTOR = EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, EntityClassification.MISC)
			// Giving it growth substance works for now as it's the only lingering substance, however this should be changed in the future.
			.setCustomClientFactory((spawnEntity, world) ->
					new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), new GrowthSubstance()))
			.build(LINGERING_EFFECTOR_ID);

	@SubscribeEvent
	public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
		IForgeRegistry<EntityType<?>> registry = entityRegistryEvent.getRegistry();
		
		registry.registerAll(FALLING_TREE.setRegistryName(DynamicTrees.resLoc(FALLING_TREE_ID)),
				LINGERING_EFFECTOR.setRegistryName(DynamicTrees.resLoc(LINGERING_EFFECTOR_ID)));
	}
	
	///////////////////////////////////////////
	// TILE ENTITIES
	///////////////////////////////////////////
	
	public static TileEntityType<SpeciesTileEntity> speciesTE;
	public static TileEntityType<PottedSaplingTileEntity> bonsaiTE;
	
	public static void setupTileEntities() {
		LinkedList<RootyBlock> rootyDirts = RootyBlockHelper.generateListForRegistry(false);
		speciesTE = TileEntityType.Builder.of(SpeciesTileEntity::new, rootyDirts.toArray(new RootyBlock[0])).build(null);
		bonsaiTE = TileEntityType.Builder.of(PottedSaplingTileEntity::new, POTTED_SAPLING).build(null);
	}
	
	@SubscribeEvent
	public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
		setupTileEntities();
		
		tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(PottedSaplingBlock.REG_NAME));
		tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(DynamicTrees.resLoc("tile_entity_species")));
	}
	
	///////////////////////////////////////////
	// WORLD GEN
	///////////////////////////////////////////

	public static final DynamicTreeFeature DYNAMIC_TREE_FEATURE = new DynamicTreeFeature();

	@SubscribeEvent
	public static void onFeatureRegistry (final RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(DYNAMIC_TREE_FEATURE);
	}

	/** The vanilla tree canceller, which cancels any features whose config extends {@link BaseTreeFeatureConfig}. */
	public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.resLoc("tree"), BaseTreeFeatureConfig.class);

	/** The vanilla fungus canceller, which cancels any features whose config extends {@link HugeFungusConfig}. */
	public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.resLoc("fungus"), HugeFungusConfig.class);

	/** The vanilla mushroom canceller, which cancels any features whose config extends {@link BigMushroomFeatureConfig}. */
	public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.resLoc("mushroom"), BigMushroomFeatureConfig.class);

	@SubscribeEvent
	public static void onFeatureCancellerRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<FeatureCanceller> event) {
		event.getRegistry().registerAll(TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
	}

	///////////////////////////////////////////
	// CUSTOM TREE LOGIC
	///////////////////////////////////////////

	@SubscribeEvent
	public static void onCellKitRegistry (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<CellKit> event) {
		CellKits.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void onGrowthLogicKitRegistry (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GrowthLogicKit> event) {
		GrowthLogicKits.register(event.getRegistry());
	}

	@SubscribeEvent
	public static void onGenFeatureRegistry (final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GenFeature> event) {
		GenFeatures.register(event.getRegistry());
	}

}
