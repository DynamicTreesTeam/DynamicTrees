package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.item.Staff;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jspecify.annotations.Nullable;

public record StaffHandleItemTintSource() implements ItemTintSource {
    public static final StaffHandleItemTintSource INSTANCE = new StaffHandleItemTintSource();
    public static final MapCodec<StaffHandleItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        int color = 0xFF5b472f; // Original brown wood color

        Species species = Staff.getSpecies(itemStack);

        if (itemStack.has(DTRegistries.STAFF_HANDLE_COLOR_DATA_COMPONENT.get())) {
            color = itemStack.getOrDefault(DTRegistries.STAFF_HANDLE_COLOR_DATA_COMPONENT.get(), new DyedItemColor(color)).rgb();
        } else if (species.isValid()) {
            color = species.getFamily().woodBarkColor;
        }

        return color;
    }


    public MapCodec<StaffHandleItemTintSource> type() {
        return MAP_CODEC;
    }

}
