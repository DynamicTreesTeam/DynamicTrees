package com.dtteam.dynamictrees.loot.function;

import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public final class MultiplyByTotalVolume extends LootItemConditionalFunction {

    public static final MapCodec<MultiplyByTotalVolume> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .apply(instance, MultiplyByTotalVolume::new));

    public MultiplyByTotalVolume(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        final Integer volume = context.getOptionalParameter(DTLootContextParams.VOLUME);
        assert volume != null;
        float multiplier = (float) volume / NetVolumeNode.Volume.VOXELSPERLOG;
        stack.setCount(Math.round(stack.getCount() * multiplier));
        return stack;
    }

    public static LootItemFunction.Builder multiplyByTotalVolume() {
        return () -> new MultiplyByTotalVolume(List.of());
    }

}
