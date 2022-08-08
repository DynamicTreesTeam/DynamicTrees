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
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class IsSpecies implements ILootCondition {

    private final ResourceLocation name;

    public IsSpecies(ResourceLocation name) {
        this.name = name;
    }

    @Override
    public LootConditionType getType() {
        return DTLootConditions.IS_SPECIES;
    }

    @Override
    public boolean test(LootContext context) {
        final Species species = context.getParamOrNull(DTLootParameters.SPECIES);
        assert species != null;
        return species.getRegistryName().equals(name);
    }

    public static ILootCondition.IBuilder isSpecies(ResourceLocation name) {
        return () -> new IsSpecies(name);
    }

    public static class Serializer implements ILootSerializer<IsSpecies> {
        @Override
        public void serialize(JsonObject json, IsSpecies value, JsonSerializationContext context) {
            json.addProperty("name", String.valueOf(value.name));
        }

        @Override
        public IsSpecies deserialize(JsonObject json, JsonDeserializationContext context) {
            return new IsSpecies(new ResourceLocation(JSONUtils.getAsString(json, "name")));
        }
    }

}
