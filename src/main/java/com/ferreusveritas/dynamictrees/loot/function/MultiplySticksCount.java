package com.ferreusveritas.dynamictrees.loot.function;

import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * @author Harley O'Connor
 */
public final class MultiplySticksCount extends LootItemConditionalFunction {

    public MultiplySticksCount(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return DTLootFunctions.MULTIPLY_STICKS_COUNT;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        final Integer volume = context.getParamOrNull(DTLootContextParams.VOLUME);
        assert volume != null;
        stack.setCount(stack.getCount() * 8 * (volume % NetVolumeNode.Volume.VOXELSPERLOG) /
                NetVolumeNode.Volume.VOXELSPERLOG);
        return stack;
    }

    public static LootItemFunction.Builder multiplySticksCount() {
        return () -> new MultiplySticksCount(new LootItemCondition[0]);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<MultiplySticksCount> {
        @Override
        public void serialize(JsonObject json, MultiplySticksCount value, JsonSerializationContext context) {
            super.serialize(json, value, context);
        }

        @Override
        public MultiplySticksCount deserialize(JsonObject json, JsonDeserializationContext context,
                                               LootItemCondition[] conditions) {
            return new MultiplySticksCount(conditions);
        }
    }
}
