package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.block.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.entity.PottedSaplingBlockEntity;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.cell.CellKits;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entity.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.item.DendroPotion;
import com.ferreusveritas.dynamictrees.item.DirtBucket;
import com.ferreusveritas.dynamictrees.item.Staff;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatures;
import com.ferreusveritas.dynamictrees.worldgen.*;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.MushroomFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.TreeFeatureCanceller;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {

    /**
     * This is the creative tab that holds all DT items. Must be instantiated here so that it's not {@code null} when we
     * create blocks and items.
     */
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(DynamicTrees.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1);
        }
    };

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, DynamicTrees.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, DynamicTrees.MOD_ID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, DynamicTrees.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, DynamicTrees.MOD_ID);

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

    /**
     * A potted sapling block, which is a normal pot but for dynamic saplings.
     */
    public static final Supplier<PottedSaplingBlock> POTTED_SAPLING = Suppliers.memoize(PottedSaplingBlock::new);

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final Supplier<TrunkShellBlock> TRUNK_SHELL = Suppliers.memoize(TrunkShellBlock::new);

    public static void setup() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        FEATURES.register(modBus);
        CONFIGURED_FEATURES.register(modBus);
        PLACED_FEATURES.register(modBus);

        setupBlocks();
        setupConnectables();
        setupItems();
    }

    private static void setupBlocks() {
        RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
        RegistryHandler.addBlock(DynamicTrees.location("trunk_shell"), TRUNK_SHELL);
    }

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                return 1;
            }
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = level.getBlockState(pos.relative(Direction.UP));
                BranchBlock branch = TreeHelper.getBranch(branchState);
                if (branch != null) {
                    return Mth.clamp(branch.getRadius(branchState) - 1, 1, 8);
                } else {
                    return 8;
                }
            }
            return 0;
        });
    }

    ///////////////////////////////////////////
    // ITEMS
    ///////////////////////////////////////////

    /**
     * A custom potion called the Dendro Potion, houses all tree potions.
     */
    public static final Supplier<DendroPotion> DENDRO_POTION = Suppliers.memoize(DendroPotion::new);

    /**
     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
     */
    public static final Supplier<DirtBucket> DIRT_BUCKET = Suppliers.memoize(DirtBucket::new);

    /**
     * A staff, a creative tool for copying and pasting tree shapes.
     */
    public static final Supplier<Staff> STAFF = Suppliers.memoize(Staff::new);

    private static void setupItems() {
        RegistryHandler.addItem(DynamicTrees.location("staff"), STAFF);
        RegistryHandler.addItem(DynamicTrees.location("dirt_bucket"), DIRT_BUCKET);
        RegistryHandler.addItem(DynamicTrees.location("dendro_potion"), DENDRO_POTION);
    }

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = registerEntity("falling_tree", () -> EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, level) -> new FallingTreeEntity(level)));
    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
            .setCustomClientFactory((spawnEntity, level) ->
                    new LingeringEffectorEntity(level, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));

    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(name));
    }

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static BlockEntityType<SpeciesBlockEntity> SPECIES_BLOCK_ENTITY;
    public static BlockEntityType<PottedSaplingBlockEntity> POTTED_SAPLING_BLOCK_ENTITY;

    public static void setupTileEntities() {
        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toArray(RootyBlock[]::new);

        SPECIES_BLOCK_ENTITY = BlockEntityType.Builder.of(SpeciesBlockEntity::new, rootyBlocks).build(null);
        POTTED_SAPLING_BLOCK_ENTITY = BlockEntityType.Builder.of(PottedSaplingBlockEntity::new, POTTED_SAPLING.get()).build(null);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegistryEvent.Register<BlockEntityType<?>> tileEntityRegistryEvent) {
        setupTileEntities();

        tileEntityRegistryEvent.getRegistry().register(POTTED_SAPLING_BLOCK_ENTITY.setRegistryName(PottedSaplingBlock.REG_NAME));
        tileEntityRegistryEvent.getRegistry().register(SPECIES_BLOCK_ENTITY.setRegistryName(DynamicTrees.location("tile_entity_species")));
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

    public static final RegistryObject<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = FEATURES.register("tree", DynamicTreeFeature::new);
    public static final RegistryObject<CaveRootedTreeFeature> CAVE_ROOTED_TREE_FEATURE = FEATURES.register("cave_rooted_tree", CaveRootedTreeFeature::new);

    public static final RegistryObject<ConfiguredFeature<NoneFeatureConfiguration, ?>> DYNAMIC_TREE_CONFIGURED_FEATURE = CONFIGURED_FEATURES.register("dynamic_tree",
            () -> new ConfiguredFeature<>(DYNAMIC_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    public static final RegistryObject<ConfiguredFeature<NoneFeatureConfiguration, ?>> CAVE_SURFACE_TREE_CONFIGURED_FEATURE = CONFIGURED_FEATURES.register("cave_rooted_tree",
            () -> new ConfiguredFeature<>(CAVE_ROOTED_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

    public static final RegistryObject<PlacedFeature> DYNAMIC_TREE_PLACED_FEATURE = PLACED_FEATURES.register("dynamic_tree_placed_feature",
            () -> PlacementUtils.inlinePlaced(DYNAMIC_TREE_CONFIGURED_FEATURE.getHolder().get()).value());
    /**
     * Placement for trees that generate on the surface above the target biome. This is used for trees like the azalea.
     */
    public static final RegistryObject<PlacedFeature> SURFACE_DYNAMIC_TREE_PLACED_FEATURE = PLACED_FEATURES.register("cave_rooted_tree",
            () -> PlacementUtils.inlinePlaced(CAVE_SURFACE_TREE_CONFIGURED_FEATURE.getHolder().get(), CaveRootedTreePlacement.INSTANCE, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12), RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome()).value());

    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("tree"), TreeConfiguration.class);

    public static final FeatureCanceller ROOTED_TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("rooted_tree"), RootSystemConfiguration.class);

    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.location("fungus"), HugeFungusConfiguration.class);

    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.location("mushroom"), HugeMushroomFeatureConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER, ROOTED_TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
    }

    ///////////////////////////////////////////
    // CUSTOM TREE LOGIC
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onCellKitRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<CellKit> event) {
        CellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GrowthLogicKit> event) {
        GrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GenFeature> event) {
        GenFeatures.register(event.getRegistry());
    }

}
