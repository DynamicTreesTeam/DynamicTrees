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
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.cells.CellKits;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreators;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.tileentity.PottedSaplingTileEntity;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.MushroomFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.TreeFeatureCanceller;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
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

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DynamicTrees.MOD_ID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, DynamicTrees.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, DynamicTrees.MOD_ID);

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

    /**
     * An apple fruit block.
     */
    public static final Supplier<FruitBlock> APPLE_FRUIT = Suppliers.memoize(() -> new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE))
            .setCanBoneMeal(DTConfigs.CAN_BONE_MEAL_APPLE::get));

    /**
     * A modified cocoa fruit block (for dynamic trees).
     */
    public static final Supplier<DynamicCocoaBlock> COCOA_FRUIT = Suppliers.memoize(DynamicCocoaBlock::new);

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
        CONFIGURED_FEATURES.register(modBus);
        PLACED_FEATURES.register(modBus);

        setupBlocks();
        setupConnectables();
        setupItems();
    }

    private static void setupBlocks() {
        RegistryHandler.addBlock(DynamicTrees.resLoc("apple_fruit"), APPLE_FRUIT);
        RegistryHandler.addBlock(DynamicTrees.resLoc("cocoa"), COCOA_FRUIT);
        RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
        RegistryHandler.addBlock(DynamicTrees.resLoc("trunk_shell"), TRUNK_SHELL);
    }

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, world, pos, side) -> {
            if (side == Direction.DOWN) {
                return 1;
            }
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, world, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = world.getBlockState(pos.relative(Direction.UP));
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

    @SubscribeEvent
    public static void onBlocksRegistry(final RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS,(a)->{
            final Species appleOak = Species.REGISTRY.get(DynamicTrees.resLoc("apple_oak"));
            if (appleOak.isValid()) {
                APPLE_FRUIT.get().setSpecies(appleOak);
            }
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
        RegistryHandler.addItem(DynamicTrees.resLoc("staff"), STAFF);
        RegistryHandler.addItem(DynamicTrees.resLoc("dirt_bucket"), DIRT_BUCKET);
        RegistryHandler.addItem(DynamicTrees.resLoc("dendro_potion"), DENDRO_POTION);
    }

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = registerEntity("falling_tree", () -> EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(world)));
    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
            .setCustomClientFactory((spawnEntity, world) ->
                    new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));

    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(name));
    }

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static BlockEntityType<SpeciesTileEntity> speciesTE;
    public static BlockEntityType<PottedSaplingTileEntity> bonsaiTE;

    public static void setupTileEntities() {
        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toArray(RootyBlock[]::new);

        speciesTE = BlockEntityType.Builder.of(SpeciesTileEntity::new, rootyBlocks).build(null);
        bonsaiTE = BlockEntityType.Builder.of(PottedSaplingTileEntity::new, POTTED_SAPLING.get()).build(null);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegisterEvent tileEntityRegistryEvent) {
        setupTileEntities();
        tileEntityRegistryEvent.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, (m)->
        {
            m.register(PottedSaplingBlock.REG_NAME, bonsaiTE);
            m.register(DynamicTrees.resLoc("tile_entity_species"), speciesTE);
        });
//        tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(PottedSaplingBlock.REG_NAME));
//        tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(DynamicTrees.resLoc("tile_entity_species")));
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////


//    @SubscribeEvent
//    public static void onFeatureRegistry(final RegistryEvent.Register<Feature<?>> event) {
//        event.getRegistry().register(DYNAMIC_TREE_FEATURE);
//    }

    public static DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, DynamicTrees.MOD_ID);
    public static final RegistryObject<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = FEATURES.register("dynamic_tree", DynamicTreeFeature::new);

    public static final RegistryObject<ConfiguredFeature<NoneFeatureConfiguration, ?>> DYNAMIC_TREE_CONFIGURED_FEATURE = CONFIGURED_FEATURES.register("dynamic_tree",
            () -> new ConfiguredFeature<>(DYNAMIC_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

    public static final RegistryObject<PlacedFeature> DYNAMIC_TREE_PLACED_FEATURE = PLACED_FEATURES.register("dynamic_tree_placed_feature",
            () -> new PlacedFeature(Holder.hackyErase(DYNAMIC_TREE_CONFIGURED_FEATURE.getHolder().get()), List.of()/*VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1F, 1))*/));

    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.resLoc("tree"), TreeConfiguration.class);

    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.resLoc("fungus"), HugeFungusConfiguration.class);

    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.resLoc("mushroom"), HugeMushroomFeatureConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
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

    @SubscribeEvent
    public static void onDropCreatorRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<DropCreator> event) {
        DropCreators.register(event.getRegistry());
    }

}
