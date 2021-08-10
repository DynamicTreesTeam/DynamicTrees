package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.api.treepacks.IVoidPropertyApplier;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import com.ferreusveritas.dynamictrees.util.BiomeList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Max Hyper
 */
public final class SeedSaplingRecipeGetter implements IJsonObjectGetter<SeedSaplingRecipe> {

    private final JsonPropertyApplierList<SeedSaplingRecipe> appliers = new JsonPropertyApplierList<>(SeedSaplingRecipe.class);

    public SeedSaplingRecipeGetter() {
        this.appliers.register("can_craft_sapling_to_seed", Boolean.class, SeedSaplingRecipe::setCanCraftSaplingToSeed)
                .register("can_craft_seed_to_sapling", Boolean.class, SeedSaplingRecipe::setCanCraftSeedToSapling)
                .register("sapling_to_seed_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .registerArrayApplier("sapling_to_seed_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSaplingToSeed)
                .register("seed_to_sapling_extra_ingredient", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling)
                .registerArrayApplier("seed_to_sapling_extra_ingredients", Item.class, SeedSaplingRecipe::addExtraIngredientForSeedToSapling);
    }

    @Override
    public ObjectFetchResult<SeedSaplingRecipe> get(JsonElement jsonElement) {
        AtomicReference<ObjectFetchResult<SeedSaplingRecipe>> result = new AtomicReference<>();
        if (jsonElement.isJsonPrimitive()){
            JsonObjectGetters.BLOCK.get(jsonElement)
                    .ifSuccessful(block -> result.set(new ObjectFetchResult<>(new SeedSaplingRecipe(block, block.asItem()))))
                    .otherwise(()->JsonObjectGetters.ITEM.get(jsonElement).ifSuccessful(item -> result.set(new ObjectFetchResult<>(new SeedSaplingRecipe(item)))));
            if (result.get() != null) return result.get();
        }
        return JsonObjectGetters.JSON_OBJECT.get(jsonElement).map(object -> {
            Block block = JsonHelper.getOrDefault(object, "sapling_block", Block.class, null);
            Item item = JsonHelper.getOrDefault(object, "sapling_item", Item.class, null);
            // we require at least a block or an item
            if (item == null){
                if (block == null)
                    return null;
                else
                    item = block.asItem();
            }
            SeedSaplingRecipe recipe = new SeedSaplingRecipe(block, item);

            this.appliers.applyAll(jsonElement.getAsJsonObject(), recipe);

            return recipe;
        }, "Error de-serialising sapling recipe from element \"" + jsonElement + "\".");
    }

}
