package com.dtteam.dynamictrees.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 26.2 {@link ItemStack} construction reads bound item components. Tree packs apply
 * {@code primitive_log} / fruit {@code item_stack} during mod init, before freeze.
 */
public final class DeferredItemStacks {

    private record Pending(Consumer<ItemStack> setter, ItemLike item) {}

    private static final List<Pending> PENDING = new CopyOnWriteArrayList<>();

    private DeferredItemStacks() {}

    public static ItemStack of(ItemLike item) {
        if (item == null) {
            return ItemStack.EMPTY;
        }
        try {
            return new ItemStack(item);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    public static void setWhenBound(Consumer<ItemStack> setter, ItemLike item) {
        if (item == null) {
            setter.accept(ItemStack.EMPTY);
            return;
        }
        try {
            setter.accept(new ItemStack(item));
        } catch (RuntimeException e) {
            PENDING.add(new Pending(setter, item));
        }
    }

    public static void flush() {
        if (PENDING.isEmpty()) {
            return;
        }
        for (Pending pending : PENDING) {
            try {
                pending.setter.accept(new ItemStack(pending.item));
            } catch (RuntimeException ignored) {
            }
        }
        PENDING.clear();
    }
}
