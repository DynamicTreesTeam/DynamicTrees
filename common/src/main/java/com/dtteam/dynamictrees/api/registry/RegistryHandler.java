package com.dtteam.dynamictrees.api.registry;

import com.dtteam.dynamictrees.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.function.Suppliers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class RegistryHandler extends RegistryEntry<RegistryHandler> {

    public static final ConcurrentRegistry<RegistryHandler> REGISTRY = new ConcurrentRegistry<>(RegistryHandler.class, Services.REGISTRY.newRegistryHandler("null"), true);

    public RegistryHandler(final ResourceLocation modId) {
        super(modId);
    }

    @Nullable
    public abstract Supplier<Block> getBlock(final ResourceLocation registryName);

    @Nullable
    public abstract Supplier<Item> getItem(final ResourceLocation registryName);

    public abstract  <T extends Block> Supplier<T> putBlock(final ResourceLocation registryName, final Supplier<T> blockSup);

    public abstract  <T extends Item> Supplier<T> putItem(final ResourceLocation registryName, final Supplier<T> itemSup);

}
