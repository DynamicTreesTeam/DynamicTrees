package com.dtteam.dynamictrees.init;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedList;
import java.util.Set;
import java.util.function.Supplier;

public abstract class RegistryLoader {

    abstract public Supplier<CreativeModeTab> registerCreativeTab (String name, ItemStack icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems);
    abstract public <T extends Block> Supplier<T> registerBlock (ResourceLocation resLoc, com.google.common.base.Supplier<T> newBlock);
    abstract public <T extends Item> Supplier<T> registerItem (ResourceLocation resLoc, com.google.common.base.Supplier<T> newBlock);
    abstract public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree);
    abstract public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks);

}
