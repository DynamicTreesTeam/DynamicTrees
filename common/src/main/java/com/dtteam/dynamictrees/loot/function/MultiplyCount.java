package com.dtteam.dynamictrees.loot.function;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public final class MultiplyCount extends LootItemConditionalFunction {

    public static final MapCodec<MultiplyCount> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .and(Codec.FLOAT.fieldOf("multiplier").forGetter(c->c.multiplier))
                    .apply(instance, MultiplyCount::new));

    private final float multiplier;

    public MultiplyCount(List<LootItemCondition> conditions, float multiplier) {
        super(conditions);
        this.multiplier = multiplier;
    }

    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.setCount((int) (stack.getCount() * multiplier));
        return stack;
    }

    public static LootItemFunction.Builder multiplyCount() {
        return () -> new MultiplyCount(List.of(), 1.0F);
    }

}
