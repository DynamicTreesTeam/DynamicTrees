package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class SeedSaplingRecipeDeserialiser implements JsonDeserialiser<SeedSaplingRecipe> {

    private final JsonPropertyAppliers<SeedSaplingRecipe> appliers = new JsonPropertyAppliers<>(SeedSaplingRecipe.class);

    public SeedSaplingRecipeDeserialiser() {
        this.appliers
                .register("can_craft_sapling_to_seed", Boolean.class, SeedSaplingRecipe::setCanCraftSaplingToSeed)
                .register("can_craft_seed_to_sapling", Boolean.class, SeedSaplingRecipe::setCanCraftSeedToSapling)
                .register("sapling_to_seed_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .registerArrayApplier("sapling_to_seed_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .register("seed_to_sapling_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling)
                .registerArrayApplier("seed_to_sapling_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling);
    }

    @Override
    public Result<SeedSaplingRecipe, JsonElement> deserialise(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(Block.class, block -> new SeedSaplingRecipe(block, block.asItem()))
                .elseMapIfType(Item.class, (Result.SimpleMapper<Item, SeedSaplingRecipe>) SeedSaplingRecipe::new)
                .elseMapIfType(JsonObject.class, (object, warningConsumer) ->
                        JsonResult.from(SeedSaplingRecipe.CODEC.decode(JsonOps.INSTANCE, input), input)
                        .map(recipe -> {
                            this.appliers.applyAll(new JsonMapWrapper(object), recipe)
                                    .forEachErrorWarning(warningConsumer, warningConsumer);
                            return recipe;
                        })
                        .orElseThrow());
    }

}
