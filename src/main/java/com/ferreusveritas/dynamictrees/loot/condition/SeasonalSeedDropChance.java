package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;

/**
 * @author Harley O'Connor
 */
public final class SeasonalSeedDropChance implements ILootCondition {

    private static final SeasonalSeedDropChance INSTANCE = new SeasonalSeedDropChance();

    private SeasonalSeedDropChance() {}

    @Override
    public LootConditionType getType() {
        return DTLootConditions.SEASONAL_SEED_DROP_CHANCE;
    }

    @Override
    public boolean test(LootContext context) {
        final Float seasonalSeedDropFactor = context.getParamOrNull(DTLootParameters.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        return seasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static ILootCondition.IBuilder seasonalSeedDropChance() {
        return () -> INSTANCE;
    }

    public static class Serializer implements ILootSerializer<SeasonalSeedDropChance> {
        public void serialize(JsonObject json, SeasonalSeedDropChance value, JsonSerializationContext context) {
        }

        public SeasonalSeedDropChance deserialize(JsonObject json, JsonDeserializationContext context) {
            return SeasonalSeedDropChance.INSTANCE;
        }
    }

}
