package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

/**
 * @author Harley O'Connor
 */
public final class VoluntarySeedDropChance implements ILootCondition {

    private final float rarity;

    public VoluntarySeedDropChance(float rarity) {
        this.rarity = rarity;
    }

    @Override
    public LootConditionType getType() {
        return DTLootConditions.VOLUNTARY_SEED_DROP_CHANCE;
    }

    @Override
    public boolean test(LootContext context) {
        final Float seasonalSeedDropFactor = context.getParamOrNull(DTLootParameters.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        return rarity * DTConfigs.SEED_DROP_RATE.get() * seasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static ILootCondition.IBuilder voluntarySeedDropChance() {
        return () -> new VoluntarySeedDropChance(1.0F);
    }

    public static ILootCondition.IBuilder voluntarySeedDropChance(float rarity) {
        return () -> new VoluntarySeedDropChance(rarity);
    }

    public static class Serializer implements ILootSerializer<VoluntarySeedDropChance> {
        @Override
        public void serialize(JsonObject json, VoluntarySeedDropChance value, JsonSerializationContext context) {
            json.addProperty("rarity", value.rarity);
        }

        @Override
        public VoluntarySeedDropChance deserialize(JsonObject json, JsonDeserializationContext context) {
            return new VoluntarySeedDropChance(JSONUtils.getAsFloat(json, "rarity", 1.0F));
        }
    }
}
