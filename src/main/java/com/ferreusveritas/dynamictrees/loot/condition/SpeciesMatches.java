package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.loot.DTLootParameters;
import com.ferreusveritas.dynamictrees.trees.Species;
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
public final class SpeciesMatches implements ILootCondition {

    private final String regex;

    public SpeciesMatches(String regex) {
        this.regex = regex;
    }

    @Override
    public LootConditionType getType() {
        return DTLootConditions.SPECIES_MATCHES;
    }

    @Override
    public boolean test(LootContext context) {
        final Species species = context.getParamOrNull(DTLootParameters.SPECIES);
        assert species != null;
        return String.valueOf(species.getRegistryName()).matches(regex);
    }

    public static ILootCondition.IBuilder speciesMatches(String regex) {
        return () -> new SpeciesMatches(regex);
    }

    public static class Serializer implements ILootSerializer<SpeciesMatches> {
        @Override
        public void serialize(JsonObject json, SpeciesMatches value, JsonSerializationContext context) {
            json.addProperty("name", String.valueOf(value.regex));
        }

        @Override
        public SpeciesMatches deserialize(JsonObject json, JsonDeserializationContext context) {
            return new SpeciesMatches(JSONUtils.getAsString(json, "name"));
        }
    }

}
