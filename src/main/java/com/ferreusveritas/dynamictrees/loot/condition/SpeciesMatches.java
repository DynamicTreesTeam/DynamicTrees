package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.loot.DTLootContextParams;
import com.ferreusveritas.dynamictrees.tree.species.Species;
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
public final class SpeciesMatches implements LootItemCondition {

    private final String regex;

    public SpeciesMatches(String regex) {
        this.regex = regex;
    }

    @Override
    public LootItemConditionType getType() {
        return DTLootConditions.SPECIES_MATCHES;
    }

    @Override
    public boolean test(LootContext context) {
        final Species species = context.getParamOrNull(DTLootContextParams.SPECIES);
        assert species != null;
        return String.valueOf(species.getRegistryName()).matches(regex);
    }

    public static LootItemCondition.Builder speciesMatches(String regex) {
        return () -> new SpeciesMatches(regex);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SpeciesMatches> {
        @Override
        public void serialize(JsonObject json, SpeciesMatches value, JsonSerializationContext context) {
            json.addProperty("name", String.valueOf(value.regex));
        }

        @Override
        public SpeciesMatches deserialize(JsonObject json, JsonDeserializationContext context) {
            return new SpeciesMatches(GsonHelper.getAsString(json, "name"));
        }
    }

}
