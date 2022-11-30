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
public final class MultiplyLogsCount extends LootItemConditionalFunction {

    public MultiplyLogsCount(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return DTLootFunctions.MULTIPLY_LOGS_COUNT;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        final Integer volume = context.getParamOrNull(DTLootContextParams.VOLUME);
        assert volume != null;
        stack.setCount(stack.getCount() * (int) Math.floor((float) volume / NetVolumeNode.Volume.VOXELSPERLOG));
        return stack;
    }

    public static LootItemFunction.Builder multiplyLogsCount() {
        return () -> new MultiplyLogsCount(new LootItemCondition[0]);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<MultiplyLogsCount> {
        @Override
        public void serialize(JsonObject json, MultiplyLogsCount value, JsonSerializationContext context) {
            super.serialize(json, value, context);
        }

        @Override
        public MultiplyLogsCount deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new MultiplyLogsCount(conditions);
        }
    }
}
