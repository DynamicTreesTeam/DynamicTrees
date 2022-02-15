package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.blocks.DynamicCocoaBlock;
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
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.MushroomFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.cancellers.TreeFeatureCanceller;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.BigMushroomFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HugeFungusConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
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
    public static final ItemGroup ITEM_GROUP = new ItemGroup(DynamicTrees.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1);
        }
    };

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

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
                    return MathHelper.clamp(branch.getRadius(branchState) - 1, 1, 8);
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

    public static final EntityType<FallingTreeEntity> FALLING_TREE = EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, EntityClassification.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(world))
            .build(FALLING_TREE_ID);
    public static final EntityType<LingeringEffectorEntity> LINGERING_EFFECTOR = EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, EntityClassification.MISC)
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

    public static TileEntityType<SpeciesTileEntity> speciesTE;
    public static TileEntityType<PottedSaplingTileEntity> bonsaiTE;

    public static void setupTileEntities() {
        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toArray(RootyBlock[]::new);

        speciesTE = TileEntityType.Builder.of(SpeciesTileEntity::new, rootyBlocks).build(null);
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
    public static void onFeatureRegistry(final RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().register(DYNAMIC_TREE_FEATURE);
    }

    public static final ConfiguredFeature<NoFeatureConfig, ?> DYNAMIC_TREE_CONFIGURED_FEATURE = DYNAMIC_TREE_FEATURE.configured(NoFeatureConfig.INSTANCE);

    public static void registerConfiguredFeatures() {
        Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, DynamicTrees.resLoc("dynamic_tree"), DYNAMIC_TREE_CONFIGURED_FEATURE);
    }

    /**
     * The vanilla tree canceller, which cancels any features whose config extends {@link BaseTreeFeatureConfig}.
     */
    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.resLoc("tree"), BaseTreeFeatureConfig.class);

    /**
     * The vanilla fungus canceller, which cancels any features whose config extends {@link HugeFungusConfig}.
     */
    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.resLoc("fungus"), HugeFungusConfig.class);

    /**
     * The vanilla mushroom canceller, which cancels any features whose config extends {@link
     * BigMushroomFeatureConfig}.
     */
    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.resLoc("mushroom"), BigMushroomFeatureConfig.class);

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
