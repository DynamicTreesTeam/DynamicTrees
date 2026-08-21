package com.dtteam.dynamictrees.registry;

import net.minecraft.resources.Identifier;

/**
 * 26.2 requires {@code Properties.setId} before constructing a Block or Item.
 * Registry helpers set these thread locals so mixins can apply the id at construction time.
 */
public final class PendingRegistryIds {

    public static final ThreadLocal<Identifier> BLOCK = new ThreadLocal<>();
    public static final ThreadLocal<Identifier> ITEM = new ThreadLocal<>();

    private PendingRegistryIds() {}
}
