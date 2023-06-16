package com.ferreusveritas.dynamictrees.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * @author Harley O'Connor
 */
public final class MultiplyCount extends LootItemConditionalFunction {

    private final float multiplier;

    public MultiplyCount(LootItemCondition[] conditions, float multiplier) {
        super(conditions);
        this.multiplier = multiplier;
    }

    @Override
    public LootItemFunctionType getType() {
        return DTLootFunctions.MULTIPLY_COUNT.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        stack.setCount((int) (stack.getCount() * multiplier));
        return stack;
    }

    public static LootItemFunction.Builder multiplyCount() {
        return () -> new MultiplyCount(new LootItemCondition[0], 1.0F);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<MultiplyCount> {
        @Override
        public void serialize(JsonObject json, MultiplyCount value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            json.addProperty("multiplier", value.multiplier);
        }

        @Override
        public MultiplyCount deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new MultiplyCount(conditions, GsonHelper.getAsFloat(json, "multiplier"));
        }
    }

}
