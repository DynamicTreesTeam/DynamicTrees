package com.dtteam.dynamictrees.client.tint;

import com.dtteam.dynamictrees.item.DendroPotion;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record DendroPotionTintSource() implements ItemTintSource {
    public static final MapCodec<DendroPotionTintSource> MAP_CODEC = MapCodec.unit(new DendroPotionTintSource());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        if (stack.getItem() instanceof DendroPotion potion) {
            return potion.getColor(stack, 0);
        }
        return 0xFFFFFFFF;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
