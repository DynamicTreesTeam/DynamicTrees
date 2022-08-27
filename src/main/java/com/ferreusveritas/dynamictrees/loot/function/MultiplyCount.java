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
import net.minecraft.util.JSONUtils;

/**
 * @author Harley O'Connor
 */
public final class MultiplyCount extends LootFunction {

    private final float multiplier;

    public MultiplyCount(ILootCondition[] conditions, float multiplier) {
        super(conditions);
        this.multiplier = multiplier;
    }

    @Override
    public LootFunctionType getType() {
        return DTLootFunctions.MULTIPLY_COUNT;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.setCount((int) (stack.getCount() * multiplier));
        return stack;
    }

    public static ILootFunction.IBuilder multiplyCount() {
        return () -> new MultiplyLogsCount(new ILootCondition[0]);
    }

    public static class Serializer extends LootFunction.Serializer<MultiplyCount> {
        @Override
        public void serialize(JsonObject json, MultiplyCount value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            json.addProperty("multiplier", value.multiplier);
        }

        @Override
        public MultiplyCount deserialize(JsonObject json, JsonDeserializationContext context, ILootCondition[] conditions) {
            return new MultiplyCount(conditions, JSONUtils.getAsFloat(json, "multiplier"));
        }
    }

}
