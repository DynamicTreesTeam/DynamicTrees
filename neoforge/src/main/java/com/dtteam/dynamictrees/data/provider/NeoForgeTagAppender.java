package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.data.tags.TagAppender;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

/**
 * Adapts vanilla 26.2 {@link net.minecraft.data.tags.TagAppender} (ResourceKey-based)
 * to DT's loader-agnostic {@link TagAppender} used by tree-pack generators.
 */
public final class NeoForgeTagAppender<T> implements TagAppender<T> {

    private final net.minecraft.data.tags.TagAppender<T> inner;
    private final ResourceKey<? extends Registry<T>> registry;
    private final Function<T, ResourceKey<T>> toKey;

    private NeoForgeTagAppender(net.minecraft.data.tags.TagAppender<T> inner,
                                ResourceKey<? extends Registry<T>> registry,
                                Function<T, ResourceKey<T>> toKey) {
        this.inner = inner;
        this.registry = registry;
        this.toKey = toKey;
    }

    public static TagAppender<Block> blocks(net.minecraft.data.tags.TagAppender<Block> inner) {
        return new NeoForgeTagAppender<>(inner, Registries.BLOCK, block ->
                ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(block)));
    }

    public static TagAppender<Item> items(net.minecraft.data.tags.TagAppender<Item> inner) {
        return new NeoForgeTagAppender<>(inner, Registries.ITEM, item ->
                ResourceKey.create(Registries.ITEM, BuiltInRegistries.ITEM.getKey(item)));
    }

    @Override
    public TagAppender<T> add(T value) {
        this.inner.add(this.toKey.apply(value));
        return this;
    }

    @Override
    public TagAppender<T> addTag(TagKey<T> tag) {
        this.inner.addTag(tag);
        return this;
    }

    @Override
    public TagAppender<T> addOptional(Identifier id) {
        this.inner.addOptional(ResourceKey.create(this.registry, id));
        return this;
    }
}
