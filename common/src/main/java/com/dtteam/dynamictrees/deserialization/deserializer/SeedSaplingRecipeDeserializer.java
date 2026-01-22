package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonMapWrapper;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.systems.SeedSaplingRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class SeedSaplingRecipeDeserializer implements JsonDeserializer<SeedSaplingRecipe> {

    private final JsonPropertyAppliers<SeedSaplingRecipe> appliers = new JsonPropertyAppliers<>(SeedSaplingRecipe.class);

    public SeedSaplingRecipeDeserializer() {
        this.appliers
                .register("replace_sapling_when_placed", Boolean.class, SeedSaplingRecipe::setReplaceSaplingWhenPlaced)
                .register("replace_sapling_when_grown", Boolean.class, SeedSaplingRecipe::setReplaceSaplingWhenGrown)
                .register("can_craft_sapling_to_seed", Boolean.class, SeedSaplingRecipe::setCanCraftSaplingToSeed)
                .register("can_craft_seed_to_sapling", Boolean.class, SeedSaplingRecipe::setCanCraftSeedToSapling)
                .register("sapling_to_seed_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .registerArrayApplier("sapling_to_seed_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .register("seed_to_sapling_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling)
                .registerArrayApplier("seed_to_sapling_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling);
    }

    @Override
    public Result<SeedSaplingRecipe, JsonElement> deserialize(JsonElement input) {
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
