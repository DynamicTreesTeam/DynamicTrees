package com.dtteam.dynamictrees.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
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
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class RegistryLoader {

    abstract public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems);

    abstract public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> newBlock);

    abstract public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> newBlock);

    abstract public <T extends Recipe<?>> Supplier<RecipeSerializer<T>> registerRecipeType(String name, Supplier<RecipeSerializer<T>> newBlock);

    abstract public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree);

    abstract public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks);

    abstract public Supplier<SoundEvent> registerSoundEvent(String name);

    abstract public <T> Supplier<DataComponentType<T>> registerDataComponentType(String name, UnaryOperator<DataComponentType.Builder<T>> operator);

    abstract public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    Supplier<I> registerCommandArgumentType(String name, Class<A> infoClass, I argumentTypeInfo);

//    abstract public Supplier<LootItemConditionType> registerLootConditionType(String name, MapCodec<? extends LootItemCondition> serializerFactory);
//
//    abstract public Supplier<LootPoolEntryType> registerLootPoolEntryType(String name, MapCodec<? extends LootPoolEntryContainer> serializerFactory);
//
//    abstract public <L extends LootItemFunction> Supplier<LootItemFunctionType<L>> registerLootFunctionType(String name, MapCodec<L> serializerFactory);

    abstract public <T extends PlacementModifier> Supplier<PlacementModifierType<T>> registerPlacementModifierType(String name, Supplier<PlacementModifierType<T>> supplier);

    abstract public <T extends Feature<?>> Supplier<T> registerFeature(String name, Supplier<T> supplier);

    abstract public <T extends BlockStateProvider> Supplier<BlockStateProviderType<T>> registerBlockStateProviderType(String name, Supplier<BlockStateProviderType<T>> supplier);

    abstract public <T extends StructurePoolElement> Supplier<StructurePoolElementType<T>> registerStructurePoolElementType(String name, Supplier<StructurePoolElementType<T>> supplier);

}