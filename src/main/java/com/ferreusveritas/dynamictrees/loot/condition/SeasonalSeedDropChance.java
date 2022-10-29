package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * @author Harley O'Connor
 */
public final class SeasonalSeedDropChance implements LootItemCondition {

    private static final SeasonalSeedDropChance INSTANCE = new SeasonalSeedDropChance();

    private SeasonalSeedDropChance() {
    }

    @Override
    public LootItemConditionType getType() {
        return DTLootConditions.SEASONAL_SEED_DROP_CHANCE;
    }

    @Override
    public boolean test(LootContext context) {
        Float seasonalSeedDropFactor = context.getParamOrNull(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        // Adjusted to a minimum of 0.15 to ensure there are at least some seed drops all year round.
        float adjustedSeasonalSeedDropFactor = Math.min(seasonalSeedDropFactor + 0.15F, 1.0F);
       return DTConfigs.SEED_DROP_RATE.get() * adjustedSeasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static LootItemCondition.Builder seasonalSeedDropChance() {
        return () -> INSTANCE;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SeasonalSeedDropChance> {
        @Override
        public void serialize(JsonObject json, SeasonalSeedDropChance value, JsonSerializationContext context) {
        }

        @Override
        public SeasonalSeedDropChance deserialize(JsonObject json, JsonDeserializationContext context) {
            return SeasonalSeedDropChance.INSTANCE;
        }
    }

}
