package com.dtteam.dynamictrees.client.TintSources;

import com.dtteam.dynamictrees.item.DendroPotion;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record DendroPotionItemTintSource() implements ItemTintSource {

    public static final DendroPotionItemTintSource INSTANCE = new DendroPotionItemTintSource();
    public static final MapCodec<DendroPotionItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        return DendroPotion.getPotionType(itemStack).getColor();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
