package com.dtteam.dynamictrees.loot.function;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return DTRegistries.MULTIPLY_COUNT.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.setCount((int) (stack.getCount() * multiplier));
        return stack;
    }

}
