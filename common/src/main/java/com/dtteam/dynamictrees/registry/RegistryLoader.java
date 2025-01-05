package com.dtteam.dynamictrees.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.component.DataComponentType;
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

import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class RegistryLoader {

    abstract public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems);

    abstract public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> newBlock);

    abstract public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> newBlock);

    abstract public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree);

    abstract public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks);

    abstract public Supplier<SoundEvent> registerSoundEvent(String name);

    abstract public <T> Supplier<DataComponentType<T>> registerDataComponentType(String name, UnaryOperator<DataComponentType.Builder<T>> operator);

    abstract public <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    Supplier<I> registerCommandArgumentType (String name, Class<A> infoClass, I argumentTypeInfo);

}