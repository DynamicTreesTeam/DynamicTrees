package com.dtteam.dynamictrees.loot.function;

import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
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
public final class MultiplyBySticksCount extends LootItemConditionalFunction {

    public static final MapCodec<MultiplyBySticksCount> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .apply(instance, MultiplyBySticksCount::new));

    public MultiplyBySticksCount(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    protected ItemStack run(ItemStack stack, LootContext context) {
        final Integer volume = context.getOptionalParameter(DTLootContextParams.VOLUME);
        assert volume != null;
        stack.setCount(stack.getCount() * 8 * (volume % NetVolumeNode.Volume.VOXELSPERLOG) /
                NetVolumeNode.Volume.VOXELSPERLOG);
        return stack;
    }

    public static LootItemFunction.Builder multiplyBySticksCount() {
        return () -> new MultiplyBySticksCount(List.of());
    }

}
