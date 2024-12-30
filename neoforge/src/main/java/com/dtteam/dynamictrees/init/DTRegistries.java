package com.dtteam.dynamictrees.init;

import com.dtteam.dynamictrees.DynamicTreesCommon;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

import java.util.LinkedList;
import java.util.function.Supplier;

@EventBusSubscriber(modid = DynamicTreesCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DTRegistries {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.HOLDER_SET_TYPES, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPES = DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, DynamicTreesCommon.MOD_ID);
    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, DynamicTreesCommon.MOD_ID);

    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();
    public static final Supplier<CreativeModeTab> DT_CREATIVE_TAB = CREATIVE_MODE_TABS.register(DynamicTreesCommon.MOD_ID, () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(Items.STICK))//TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1))
            .title(Component.translatable("itemGroup.dynamictrees"))
            .displayItems((parameters, output) -> {
                output.accept(Items.STICK);
//                for (final DendroPotion.DendroPotionType potion : DendroPotion.DendroPotionType.values()) {
//                    if (potion.isActive()) {
//                        output.accept(DendroPotion.applyIndexTag(new ItemStack(DTRegistries.DENDRO_POTION.get()), potion.getIndex()));
//                    }
//                }
                CREATIVE_TAB_ITEMS.forEach(e -> output.accept(e.getDefaultInstance()));
            }).build());

    public static void setup(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        FEATURES.register(modBus);
        PLACEMENT_MODIFIER_TYPES.register(modBus);
        SOUND_EVENTS.register(modBus);
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        HOLDER_SET_TYPES.register(modBus);
        BLOCK_STATE_PROVIDER_TYPES.register(modBus);
        STRUCTURE_POOL_ELEMENT_TYPES.register(modBus);
//        DTLootPoolEntries.LOOT_POOL_ENTRY_TYPES.register(modBus);
//        DTLootConditions.LOOT_CONDITION_TYPES.register(modBus);
//        DTLootFunctions.LOOT_FUNCTION_TYPES.register(modBus);

        setupBlocks();
        setupConnectables();
        setupItems();
    }

//    ///////////////////////////////////////////
//    // BLOCKS
//    ///////////////////////////////////////////
//
//    /**
//     * A potted sapling block, which is a normal pot but for dynamic saplings.
//     */
//    public static final Supplier<PottedSaplingBlock> POTTED_SAPLING = Suppliers.memoize(PottedSaplingBlock::new);

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final Supplier<TrunkShellBlock> TRUNK_SHELL = Suppliers.memoize(TrunkShellBlock::new);

    private static void setupBlocks() {
//        RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
//        RegistryHandler.addBlock(DynamicTreesCommon.location("trunk_shell"), TRUNK_SHELL);
    }

    private static void setupConnectables() {
//        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, level, pos, side) -> {
//            if (side == Direction.DOWN) {
//                return 1;
//            }
//            return 0;
//        });
//
//        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, level, pos, side) -> {
//            if (side == Direction.DOWN) {
//                BlockState branchState = level.getBlockState(pos.relative(Direction.UP));
//                BranchBlock branch = TreeHelper.getBranch(branchState);
//                if (branch != null) {
//                    return Mth.clamp(branch.getRadius(branchState) - 1, 1, 8);
//                } else {
//                    return 8;
//                }
//            }
//            return 0;
//        });
    }

//    ///////////////////////////////////////////
//    // ITEMS
//    ///////////////////////////////////////////
//
//    /**
//     * A custom potion called the Dendro Potion, houses all tree potions.
//     */
//    public static final Supplier<DendroPotion> DENDRO_POTION = Suppliers.memoize(DendroPotion::new);
//
//    /**
//     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
//     */
//    public static final Supplier<DirtBucket> DIRT_BUCKET = Suppliers.memoize(DirtBucket::new);
//
//    /**
//     * A staff, a creative tool for copying and pasting tree shapes.
//     */
//    public static final Supplier<Staff> STAFF = Suppliers.memoize(Staff::new);
//
    private static void setupItems() {
//        RegistryHandler.addItem(DynamicTreesCommon.location("staff"), STAFF);
//        RegistryHandler.addItem(DynamicTreesCommon.location("dirt_bucket"), DIRT_BUCKET);
//        RegistryHandler.addItem(DynamicTreesCommon.location("dendro_potion"), DENDRO_POTION);
    }
//
//    ///////////////////////////////////////////
//    // ENTITIES
//    ///////////////////////////////////////////
//
//    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = registerEntity("falling_tree", () -> EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
//            .setShouldReceiveVelocityUpdates(true)
//            .setTrackingRange(512)
//            .setUpdateInterval(Integer.MAX_VALUE)
//            .setCustomClientFactory((spawnEntity, level) -> new FallingTreeEntity(level)));
//    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
//            .setCustomClientFactory((spawnEntity, level) ->
//                    new LingeringEffectorEntity(level, BlockPos.containing(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));
//
//    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
//        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(name));
//    }
//
//    ///////////////////////////////////////////
//    // TILE ENTITIES
//    ///////////////////////////////////////////
//
//    public static BlockEntityType<SpeciesBlockEntity> SPECIES_BLOCK_ENTITY;
//    public static BlockEntityType<PottedSaplingBlockEntity> POTTED_SAPLING_BLOCK_ENTITY;
//
//    public static void setupTileEntities() {
//        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
//                .map(SoilProperties::getBlock)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .distinct()
//                .toArray(RootyBlock[]::new);
//
//        SPECIES_BLOCK_ENTITY = BlockEntityType.Builder.of(SpeciesBlockEntity::new, rootyBlocks).build(null);
//        POTTED_SAPLING_BLOCK_ENTITY = BlockEntityType.Builder.of(PottedSaplingBlockEntity::new, POTTED_SAPLING.get()).build(null);
//    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegisterEvent tileEntityRegistryEvent) {
//        tileEntityRegistryEvent.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, registerHelper -> {
//            setupTileEntities();
//            registerHelper.register(PottedSaplingBlock.REG_NAME, POTTED_SAPLING_BLOCK_ENTITY);
//            registerHelper.register(DynamicTreesCommon.location("tile_entity_species"), SPECIES_BLOCK_ENTITY);
//        });
    }
//
//    ///////////////////////////////////////////
//    // WORLD GEN
//    ///////////////////////////////////////////
//
//    public static final RegistryObject<PlacementModifierType<CaveRootedTreePlacement>> CAVE_ROOTED_TREE_PLACEMENT_MODIFIER_TYPE = PLACEMENT_MODIFIER_TYPES.register("cave_rooted_tree",
//            () -> () -> CaveRootedTreePlacement.CODEC);
//
//    public static final RegistryObject<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = FEATURES.register("tree", DynamicTreeFeature::new);
//    public static final RegistryObject<CaveRootedTreeFeature> CAVE_ROOTED_TREE_FEATURE = FEATURES.register("cave_rooted_tree", CaveRootedTreeFeature::new);
//
//    public static final RegistryObject<Codec<AddDynamicTreesBiomeModifier>> ADD_DYNAMIC_TREES_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("add_dynamic_trees",
//            () -> Codec.unit(AddDynamicTreesBiomeModifier::new));
//    public static final RegistryObject<Codec<RunFeatureCancellersBiomeModifier>> RUN_FEATURE_CANCELLERS_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("run_feature_cancellers",
//            () -> Codec.unit(RunFeatureCancellersBiomeModifier::new));
//    public static final RegistryObject<HolderSetType> INCLUDES_EXCLUDES_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("includes_excludes", () -> IncludesExcludesHolderSet::codec);
//    public static final RegistryObject<HolderSetType> NAME_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("name_regex_match", () -> NameRegexMatchHolderSet::codec);
//    public static final RegistryObject<HolderSetType> TAGS_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("tags_regex_match", () -> NameRegexMatchHolderSet::codec);
//
//    public static final RegistryObject<BlockStateProviderType<DTReplaceNyliumFungiBlockStateProvider>> REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE = BLOCK_STATE_PROVIDER_TYPES.register(
//        "replace_nylium_fungi", () -> new BlockStateProviderType<>(DTReplaceNyliumFungiBlockStateProvider.CODEC));
//
//    public static final RegistryObject<StructurePoolElementType<DTCancelVanillaTreePoolElement>> CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
//            "cancel_vanilla_village_tree_element", () -> () -> DTCancelVanillaTreePoolElement.CODEC);
//    public static final RegistryObject<StructurePoolElementType<TreePoolElement>> TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
//            "tree_pool_element", () -> () -> TreePoolElement.CODEC);
//
//    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTreesCommon.location("tree"), TreeConfiguration.class);
//
//    public static final FeatureCanceller ROOTED_TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTreesCommon.location("rooted_tree"), RootSystemConfiguration.class);
//
//    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTreesCommon.location("fungus"), HugeFungusConfiguration.class);
//
//    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTreesCommon.location("mushroom"), HugeMushroomFeatureConfiguration.class);
//
//    @SubscribeEvent
//    public static void onFeatureCancellerRegistry(final com.dtteam.dynamictrees.registry.RegistryEvent<FeatureCanceller> event) {
//        event.getRegistry().registerAll(TREE_CANCELLER, ROOTED_TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
//    }
//
//    ///////////////////////////////////////////
//    // CUSTOM TREE LOGIC
//    ///////////////////////////////////////////
//
//    @SubscribeEvent
//    public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
//        CellKits.register(event.getRegistry());
//    }
//
//    @SubscribeEvent
//    public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
//        GrowthLogicKits.register(event.getRegistry());
//    }
//
//    @SubscribeEvent
//    public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
//        GenFeatures.register(event.getRegistry());
//    }
//
//    ///////////////////////////////////////////
//    // SOUNDS
//    ///////////////////////////////////////////
//
//    public static final RegistryObject<SoundEvent> FALLING_TREE_HIT_WATER = registerSoundEvent("falling_tree_hit_water");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_BIG_START = registerSoundEvent("falling_tree_big_start");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_BIG_END = registerSoundEvent("falling_tree_big_end");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_MEDIUM_START = registerSoundEvent("falling_tree_medium_start");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_MEDIUM_END = registerSoundEvent("falling_tree_medium_end");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_HIT_WATER = registerSoundEvent("falling_tree_small_hit_water");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_END = registerSoundEvent("falling_tree_small_end");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_END_BARE = registerSoundEvent("falling_tree_small_end_bare");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_START = registerSoundEvent("falling_tree_fungus_start");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_END = registerSoundEvent("falling_tree_fungus_end");
//    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_SMALL_END = registerSoundEvent("falling_tree_fungus_small_end");
//
//    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
//        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(DynamicTreesCommon.location(name)));
//    }
}
