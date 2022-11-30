package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * @author Harley O'Connor
 */
public final class VoluntarySeedDropChance implements LootItemCondition {

    private final float rarity;

    public VoluntarySeedDropChance(float rarity) {
        this.rarity = rarity;
    }

    @Override
    public LootItemConditionType getType() {
        return DTLootConditions.VOLUNTARY_SEED_DROP_CHANCE;
    }

    @Override
    public boolean test(LootContext context) {
        final Float seasonalSeedDropFactor = context.getParamOrNull(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        return rarity * DTConfigs.VOLUNTARY_SEED_DROP_RATE.get() * seasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static LootItemCondition.Builder voluntarySeedDropChance() {
        return () -> new VoluntarySeedDropChance(1.0F);
    }

    public static LootItemCondition.Builder voluntarySeedDropChance(float rarity) {
        return () -> new VoluntarySeedDropChance(rarity);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<VoluntarySeedDropChance> {
        @Override
        public void serialize(JsonObject json, VoluntarySeedDropChance value, JsonSerializationContext context) {
            json.addProperty("rarity", value.rarity);
        }

        @Override
        public VoluntarySeedDropChance deserialize(JsonObject json, JsonDeserializationContext context) {
            return new VoluntarySeedDropChance(GsonHelper.getAsFloat(json, "rarity", 1.0F));
        }
    }
}
