package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FabricRegistryLoader extends RegistryLoader {

    public static void setup (){
        DTRegistries.setup();
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> newBlock) {
        T block = Registry.register(BuiltInRegistries.BLOCK, DynamicTrees.location(name), newBlock.get());
        return ()-> block;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> newBlock) {
        T item = Registry.register(BuiltInRegistries.ITEM, DynamicTrees.location(name), newBlock.get());
        return ()-> item;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems) {
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DynamicTrees.location(DynamicTrees.MOD_ID),
                FabricItemGroup.builder().icon(icon).title(title).displayItems(displayItems).build());
        return ()-> tab;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree) {
//        if (isTree)
//            builder.setShouldReceiveVelocityUpdates(true).setTrackingRange(512).setUpdateInterval(Integer.MAX_VALUE);
        EntityType<T> entityType = Registry.register(BuiltInRegistries.ENTITY_TYPE, DynamicTrees.location(name), builder.build(name));
        return ()-> entityType;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks) {
        BlockEntityType<T> entityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, DynamicTrees.location(name), new BlockEntityType<>(newBlockEntity, validBlocks.get(), null));
        return ()-> entityType;
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation location = DynamicTrees.location(name);
        SoundEvent type = Registry.register(BuiltInRegistries.SOUND_EVENT, location, SoundEvent.createVariableRangeEvent(location));
        return ()-> type;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponentType(String name, UnaryOperator<DataComponentType.Builder<T>> operator) {
        DataComponentType<T> type = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, DynamicTrees.location(name), operator.apply(DataComponentType.builder()).build());
        return ()-> type;
    }

    @Override
    public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> Supplier<I> registerCommandArgumentType(String name, Class<A> infoClass, I argumentTypeInfo) {
        ArgumentTypeInfos.BY_CLASS.put(infoClass, argumentTypeInfo);
        I type = Registry.register(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, DynamicTrees.location(name), argumentTypeInfo);
        return ()-> type;
    }

    @Override
    public Supplier<LootItemConditionType> registerLootConditionType(String name, MapCodec<? extends LootItemCondition> serializerFactory) {
        LootItemConditionType type = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, DynamicTrees.location(name), new LootItemConditionType(serializerFactory));
        return ()-> type;
    }

    @Override
    public Supplier<LootPoolEntryType> registerLootPoolEntryType(String name, MapCodec<? extends LootPoolEntryContainer> serializerFactory) {
        LootPoolEntryType type = Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, DynamicTrees.location(name), new LootPoolEntryType(serializerFactory));
        return ()-> type;
    }

    @Override
    public <L extends LootItemFunction> Supplier<LootItemFunctionType<L>> registerLootFunctionType(String name, MapCodec<L> serializerFactory) {
        LootItemFunctionType<L> type = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, DynamicTrees.location(name), new LootItemFunctionType<>(serializerFactory));
        return ()-> type;
    }

    @Override
    public <T extends PlacementModifier> Supplier<PlacementModifierType<T>> registerPlacementModifierType(String name, Supplier<PlacementModifierType<T>> supplier) {
        PlacementModifierType<T> type = Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, DynamicTrees.location(name), supplier.get());
        return ()-> type;
    }

    @Override
    public <T extends Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> supplier) {
        T feature = Registry.register(BuiltInRegistries.FEATURE, DynamicTrees.location(name), supplier.get());
        return ()-> feature;
    }

    @Override
    public <T extends BlockStateProvider> Supplier<BlockStateProviderType<T>> registerBlockStateProviderType(String name, Supplier<BlockStateProviderType<T>> supplier) {
        BlockStateProviderType<T> type = Registry.register(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, DynamicTrees.location(name), supplier.get());
        return ()-> type;
    }

    @Override
    public <T extends StructurePoolElement> Supplier<StructurePoolElementType<T>> registerStructurePoolElementType(String name, Supplier<StructurePoolElementType<T>> supplier) {
        StructurePoolElementType<T> type = Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, DynamicTrees.location(name), supplier.get());
        return ()-> type;
    }
}
