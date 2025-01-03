package com.dtteam.dynamictrees.config;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.Set;
import java.util.function.Supplier;

public class FabricRegistryLoader extends RegistryLoader {

    public static void setup (){

    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, MutableComponent title, CreativeModeTab.DisplayItemsGenerator displayItems) {
        return ()->Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DynamicTrees.location(DynamicTrees.MOD_ID),
                FabricItemGroup.builder().icon(icon).title(title).displayItems(displayItems).build()) ;
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> newBlock) {
        return null;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> newBlock) {
        return null;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder, boolean isTree) {
        return null;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<? extends T> newBlockEntity, Supplier<Set<Block>> validBlocks) {
        return null;
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name) {
        return null;
    }

}
