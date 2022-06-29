package com.ferreusveritas.dynamictrees.loot.function;

import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;

/**
 * @author Harley O'Connor
 */
public final class MultiplyLogsCount extends LootFunction {

    public MultiplyLogsCount(ILootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootFunctionType getType() {
        return DTLootFunctions.MULTIPLY_LOGS_COUNT;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        final Integer volume = context.getParamOrNull(DTLootParameters.VOLUME);
        assert volume != null;
        stack.setCount(stack.getCount() * (int) Math.floor((float) volume / NetVolumeNode.Volume.VOXELSPERLOG));
        return stack;
    }

    public static ILootFunction.IBuilder multiplyLogsCount() {
        return () -> new MultiplyLogsCount(new ILootCondition[0]);
    }

    public static class Serializer extends LootFunction.Serializer<MultiplyLogsCount> {
        public void serialize(JsonObject json, MultiplyLogsCount value, JsonSerializationContext context) {
            super.serialize(json, value, context);
        }

        public MultiplyLogsCount deserialize(JsonObject json, JsonDeserializationContext context, ILootCondition[] conditions) {
            return new MultiplyLogsCount(conditions);
        }
    }
}
