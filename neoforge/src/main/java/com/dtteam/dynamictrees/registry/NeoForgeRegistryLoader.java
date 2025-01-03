package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

import java.util.Set;
import java.util.function.Supplier;

public class NeoForgeRegistryLoader extends RegistryLoader {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynamicTrees.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, DynamicTrees.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DynamicTrees.MOD_ID);
    public static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.HOLDER_SET_TYPES, DynamicTrees.MOD_ID);
    public static final DeferredRegister<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPES = DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, DynamicTrees.MOD_ID);

    public static void setup(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register(modBus);
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

        DTRegistries.setup();
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock (String name, Supplier<T> newBlock){
        Supplier<T> sup = Suppliers.memoize(newBlock::get);
        RegistryHandler.addBlock(DynamicTrees.location(name), sup);
        return sup;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem (String name, Supplier<T> newBlock){
        Supplier<T> sup = Suppliers.memoize(newBlock::get);
        RegistryHandler.addItem(DynamicTrees.location(name), sup);
        return sup;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems) {
        return CREATIVE_MODE_TABS.register(name,
                () -> CreativeModeTab.builder().icon(icon).title(title).displayItems(displayItems).build());
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree) {
        if (isTree)
            builder.setShouldReceiveVelocityUpdates(true).setTrackingRange(512).setUpdateInterval(Integer.MAX_VALUE);
        return ENTITY_TYPES.register(name, () -> builder.build(name));
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks) {
        return BLOCK_ENTITY_TYPES.register(name, () ->
                new BlockEntityType<>(newBlockEntity, validBlocks.get(), null));
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(DynamicTrees.location(name)));
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

//    public static final Supplier<PlacementModifierType<CaveRootedTreePlacement>> CAVE_ROOTED_TREE_PLACEMENT_MODIFIER_TYPE = PLACEMENT_MODIFIER_TYPES.register("cave_rooted_tree",
//            () -> () -> CaveRootedTreePlacement.CODEC);

//    public static final Supplier<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = FEATURES.register("tree", DynamicTreeFeature::new);
//    public static final Supplier<CaveRootedTreeFeature> CAVE_ROOTED_TREE_FEATURE = FEATURES.register("cave_rooted_tree", CaveRootedTreeFeature::new);

//    public static final Supplier<Codec<AddDynamicTreesBiomeModifier>> ADD_DYNAMIC_TREES_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("add_dynamic_trees",
//            () -> Codec.unit(AddDynamicTreesBiomeModifier::new));
//    public static final Supplier<Codec<RunFeatureCancellersBiomeModifier>> RUN_FEATURE_CANCELLERS_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("run_feature_cancellers",
//            () -> Codec.unit(RunFeatureCancellersBiomeModifier::new));
//    public static final Supplier<HolderSetType> INCLUDES_EXCLUDES_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("includes_excludes", () -> IncludesExcludesHolderSet::codec);
//    public static final Supplier<HolderSetType> NAME_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("name_regex_match", () -> NameRegexMatchHolderSet::codec);
//    public static final Supplier<HolderSetType> TAGS_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("tags_regex_match", () -> NameRegexMatchHolderSet::codec);

//    public static final Supplier<BlockStateProviderType<DTReplaceNyliumFungiBlockStateProvider>> REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE = BLOCK_STATE_PROVIDER_TYPES.register(
//        "replace_nylium_fungi", () -> new BlockStateProviderType<>(DTReplaceNyliumFungiBlockStateProvider.CODEC));
//
//    public static final Supplier<StructurePoolElementType<DTCancelVanillaTreePoolElement>> CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
//            "cancel_vanilla_village_tree_element", () -> () -> DTCancelVanillaTreePoolElement.CODEC);
//    public static final Supplier<StructurePoolElementType<TreePoolElement>> TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
//            "tree_pool_element", () -> () -> TreePoolElement.CODEC);

//    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("tree"), TreeConfiguration.class);
//
//    public static final FeatureCanceller ROOTED_TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("rooted_tree"), RootSystemConfiguration.class);
//
//    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.location("fungus"), HugeFungusConfiguration.class);
//
//    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.location("mushroom"), HugeMushroomFeatureConfiguration.class);

//    @SubscribeEvent
//    public static void onFeatureCancellerRegistry(final com.dtteam.dynamictrees.registry.RegistryEvent<FeatureCanceller> event) {
//        event.getRegistry().registerAll(TREE_CANCELLER, ROOTED_TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
//    }
//
//
//    ///////////////////////////////////////////
//    // SOUNDS
//    ///////////////////////////////////////////
//
//    public static final Supplier<SoundEvent> FALLING_TREE_HIT_WATER = registerSoundEvent("falling_tree_hit_water");
//    public static final Supplier<SoundEvent> FALLING_TREE_BIG_START = registerSoundEvent("falling_tree_big_start");
//    public static final Supplier<SoundEvent> FALLING_TREE_BIG_END = registerSoundEvent("falling_tree_big_end");
//    public static final Supplier<SoundEvent> FALLING_TREE_MEDIUM_START = registerSoundEvent("falling_tree_medium_start");
//    public static final Supplier<SoundEvent> FALLING_TREE_MEDIUM_END = registerSoundEvent("falling_tree_medium_end");
//    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_HIT_WATER = registerSoundEvent("falling_tree_small_hit_water");
//    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_END = registerSoundEvent("falling_tree_small_end");
//    public static final Supplier<SoundEvent> FALLING_TREE_SMALL_END_BARE = registerSoundEvent("falling_tree_small_end_bare");
//    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_START = registerSoundEvent("falling_tree_fungus_start");
//    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_END = registerSoundEvent("falling_tree_fungus_end");
//    public static final Supplier<SoundEvent> FALLING_TREE_FUNGUS_SMALL_END = registerSoundEvent("falling_tree_fungus_small_end");
//

}
