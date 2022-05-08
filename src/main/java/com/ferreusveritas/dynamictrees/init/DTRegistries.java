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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;

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

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

    /**
     * An apple fruit block.
     */
    public static final FruitBlock APPLE_FRUIT = new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE))
            .setCanBoneMeal(DTConfigs.CAN_BONE_MEAL_APPLE::get);

    /**
     * A modified cocoa fruit block (for dynamic trees).
     */
    public static final DynamicCocoaBlock COCOA_FRUIT = new DynamicCocoaBlock();

    /**
     * A potted sapling block, which is a normal pot but for dynamic saplings.
     */
    public static final PottedSaplingBlock POTTED_SAPLING = new PottedSaplingBlock();

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final TrunkShellBlock TRUNK_SHELL = new TrunkShellBlock();

    public static void setup() {
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
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        final Species appleOak = Species.REGISTRY.get(DynamicTrees.resLoc("apple_oak"));

        if (appleOak.isValid()) {
            APPLE_FRUIT.setSpecies(appleOak);
        }
    }

    ///////////////////////////////////////////
    // ITEMS
    ///////////////////////////////////////////

    /**
     * A custom potion called the Dendro Potion, houses all tree potions.
     */
    public static final DendroPotion DENDRO_POTION = new DendroPotion();

    /**
     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
     */
    public static final DirtBucket DIRT_BUCKET = new DirtBucket();

    /**
     * A staff, a creative tool for copying and pasting tree shapes.
     */
    public static final Staff STAFF = new Staff();

    private static void setupItems() {
        RegistryHandler.addItem(DynamicTrees.resLoc("staff"), STAFF);
        RegistryHandler.addItem(DynamicTrees.resLoc("dirt_bucket"), DIRT_BUCKET);
        RegistryHandler.addItem(DynamicTrees.resLoc("dendro_potion"), DENDRO_POTION);
    }

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public final static String FALLING_TREE_ID = "falling_tree";
    public final static String LINGERING_EFFECTOR_ID = "lingering_effector";

    public static final EntityType<FallingTreeEntity> FALLING_TREE = EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(world))
            .build(FALLING_TREE_ID);
    public static final EntityType<LingeringEffectorEntity> LINGERING_EFFECTOR = EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
            .setCustomClientFactory((spawnEntity, world) ->
                    new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null))
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
        bonsaiTE = BlockEntityType.Builder.of(PottedSaplingTileEntity::new, POTTED_SAPLING).build(null);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegistryEvent.Register<BlockEntityType<?>> tileEntityRegistryEvent) {
        setupTileEntities();

        tileEntityRegistryEvent.getRegistry().register(bonsaiTE.setRegistryName(PottedSaplingBlock.REG_NAME));
        tileEntityRegistryEvent.getRegistry().register(speciesTE.setRegistryName(DynamicTrees.resLoc("tile_entity_species")));
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

    public static final DynamicTreeFeature DYNAMIC_TREE_FEATURE = new DynamicTreeFeature();

    @SubscribeEvent
    public static void onFeatureRegistry(final RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().register(DYNAMIC_TREE_FEATURE);
    }

    public static final ConfiguredFeature<NoneFeatureConfiguration, ?> DYNAMIC_TREE_CONFIGURED_FEATURE = DYNAMIC_TREE_FEATURE.configured(NoneFeatureConfiguration.INSTANCE);

    public static final PlacedFeature DYNAMIC_TREE_PLACED_FEATURE = PlacementUtils.register("dynamic_tree_placed_feature", DYNAMIC_TREE_CONFIGURED_FEATURE.placed(VegetationPlacements.TREE_THRESHOLD));


    public static void registerConfiguredFeatures() {
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, DynamicTrees.resLoc("dynamic_tree"), DYNAMIC_TREE_CONFIGURED_FEATURE);
    }


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
