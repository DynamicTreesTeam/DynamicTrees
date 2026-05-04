package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jspecify.annotations.Nullable;

public record StaffCrystalItemTintSource() implements ItemTintSource {
    public static final StaffCrystalItemTintSource INSTANCE = new StaffCrystalItemTintSource();
    public static final MapCodec<StaffCrystalItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        int color = 0xFF00FFFF; // Cyan crystal like Radagast the Brown's staff.

        if (itemStack.has(DTRegistries.STAFF_CRYSTAL_COLOR_DATA_COMPONENT.get())) {
            color = itemStack.getOrDefault(DTRegistries.STAFF_CRYSTAL_COLOR_DATA_COMPONENT.get(), new DyedItemColor(color)).rgb();
        }

        return color;
    }

    public MapCodec<StaffCrystalItemTintSource> type() {
        return MAP_CODEC;
    }

}
