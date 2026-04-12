package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.worldgen.biomemodifier.AddDynamicTreesBiomeModifier;
import com.dtteam.dynamictrees.worldgen.biomemodifier.RunFeatureCancellersBiomeModifier;
import com.dtteam.dynamictrees.worldgen.holderset.IncludesExcludesHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.NameRegexMatchHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.TagsRegexMatchHolderSet;
import com.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryLoader extends RegistryLoader {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynamicTrees.MOD_ID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, DynamicTrees.MOD_ID);
    public static final DeferredRegister<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPES = DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, DynamicTrees.MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DynamicTrees.MOD_ID);


    public static void setup(IEventBus modBus) {
        BLOCK_ENTITY_TYPES.register(modBus);
        ENTITY_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        FEATURES.register(modBus);
        PLACEMENT_MODIFIER_TYPES.register(modBus);
        SOUND_EVENTS.register(modBus);
        BLOCK_STATE_PROVIDER_TYPES.register(modBus);
        STRUCTURE_POOL_ELEMENT_TYPES.register(modBus);
        DATA_COMPONENT_TYPES.register(modBus);
        ARGUMENT_TYPES.register(modBus);
//        LOOT_POOL_ENTRY_TYPES.register(modBus);
//        LOOT_CONDITION_TYPES.register(modBus);
//        LOOT_FUNCTION_TYPES.register(modBus);
        RECIPE_SERIALIZER.register(modBus);

        //NeoForge
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        HOLDER_SET_TYPES.register(modBus);

        DTRegistries.setup();
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock (String name, Function<Identifier, T> newBlock){
        Identifier id = DynamicTrees.location(name);
        Supplier<T> sup = Suppliers.memoize(()->newBlock.apply(id));
        RegistryHandler.addBlock(id, sup);
        return sup;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem (String name, Function<Identifier, T> newItem){
        Identifier id = DynamicTrees.location(name);
        Supplier<T> sup = Suppliers.memoize(()->newItem.apply(id));
        RegistryHandler.addItem(id, sup);
        return sup;
    }

    @Override
    public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeType(String name, Supplier<RecipeSerializer<T>> newBlock) {
        return RECIPE_SERIALIZER.register(name, newBlock);
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
        return ENTITY_TYPES.register(name, () -> builder.build(ResourceKey.create(ENTITY_TYPES.getRegistryKey(), DynamicTrees.location(name))));
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks) {
        return BLOCK_ENTITY_TYPES.register(name, () ->
                new BlockEntityType<>(newBlockEntity, validBlocks.get()));
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(DynamicTrees.location(name)));
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponentType (String name, UnaryOperator<DataComponentType.Builder<T>> operator){
        return DATA_COMPONENT_TYPES.register(name, ()->operator.apply(DataComponentType.builder()).build());
    }

    @Override
    public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> Supplier<I> registerCommandArgumentType (String name, Class<A> infoClass, I argumentTypeInfo){
        return ARGUMENT_TYPES.register(name, () -> ArgumentTypeInfos.registerByClass(infoClass, argumentTypeInfo));
    }

//    @Override
//    public Supplier<LootItemConditionType> registerLootConditionType(String name, MapCodec<? extends LootItemCondition> serializerFactory) {
//        return LOOT_CONDITION_TYPES.register(name, () -> new LootItemConditionType(serializerFactory));
//    }
//
//    @Override
//    public Supplier<LootPoolEntryType> registerLootPoolEntryType(String name, MapCodec<? extends LootPoolEntryContainer> serializerFactory) {
//        return LOOT_POOL_ENTRY_TYPES.register(name, () -> new LootPoolEntryType(serializerFactory));
//    }
//
//    @Override
//    public <L extends LootItemFunction> Supplier<LootItemFunctionType<L>> registerLootFunctionType(String name, MapCodec<L> serializerFactory) {
//        return LOOT_FUNCTION_TYPES.register(name, () -> new LootItemFunctionType<>(serializerFactory));
//    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

    @Override
    public <T extends PlacementModifier> Supplier<PlacementModifierType<T>> registerPlacementModifierType (String name, Supplier<PlacementModifierType<T>> supplier){
        return PLACEMENT_MODIFIER_TYPES.register(name, supplier);
    }

    @Override
    public <T extends Feature<?>> Supplier<T> registerFeature (String name, Supplier<T> supplier){
        return FEATURES.register(name, supplier);
    }

    @Override
    public <T extends BlockStateProvider> Supplier<BlockStateProviderType<T>> registerBlockStateProviderType (String name, Supplier<BlockStateProviderType<T>> supplier){
        return BLOCK_STATE_PROVIDER_TYPES.register(name, supplier);
    }

    @Override
    public <T extends StructurePoolElement> Supplier<StructurePoolElementType<T>> registerStructurePoolElementType (String name, Supplier<StructurePoolElementType<T>> supplier){
        return STRUCTURE_POOL_ELEMENT_TYPES.register(name, supplier);
    }

    ///////////////////////////////////////////
    // NEO FORGE ONLY
    ///////////////////////////////////////////

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DynamicTrees.MOD_ID);
    public static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.HOLDER_SET_TYPES, DynamicTrees.MOD_ID);

    public static final Supplier<MapCodec<AddDynamicTreesBiomeModifier>> ADD_DYNAMIC_TREES_BIOME_MODIFIER =
            BIOME_MODIFIER_SERIALIZERS.register("add_dynamic_trees", () -> MapCodec.unit(AddDynamicTreesBiomeModifier::new));
    public static final Supplier<MapCodec<RunFeatureCancellersBiomeModifier>> RUN_FEATURE_CANCELLERS_BIOME_MODIFIER =
            BIOME_MODIFIER_SERIALIZERS.register("run_feature_cancellers", () -> MapCodec.unit(RunFeatureCancellersBiomeModifier::new));
    public static final Supplier<HolderSetType> INCLUDES_EXCLUDES_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("includes_excludes", IncludesExcludesHolderSet.Type::new);
    public static final Supplier<HolderSetType> NAME_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("name_regex_match", NameRegexMatchHolderSet.Type::new);
    public static final Supplier<HolderSetType> TAGS_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("tags_regex_match", TagsRegexMatchHolderSet.Type::new);

}
