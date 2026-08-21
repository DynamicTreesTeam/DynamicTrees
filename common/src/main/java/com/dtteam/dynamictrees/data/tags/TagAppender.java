package com.dtteam.dynamictrees.data.tags;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/**
 * Loader-agnostic tag builder used by generated tree-pack tags. NeoForge/Fabric datagen
 * adapt their provider {@code tag()} methods to this type.
 */
public interface TagAppender<T> {

    TagAppender<T> add(T value);

    TagAppender<T> addTag(TagKey<T> tag);

    TagAppender<T> addOptional(Identifier id);

}
