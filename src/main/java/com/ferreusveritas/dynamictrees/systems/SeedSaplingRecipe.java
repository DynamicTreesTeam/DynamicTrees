package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.util.Optionals;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SeedSaplingRecipe {

    @SuppressWarnings("deprecation")
    public static final Codec<SeedSaplingRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Registry.BLOCK.optionalFieldOf("sapling_block").forGetter(SeedSaplingRecipe::getSaplingBlock),
                    Registry.ITEM.optionalFieldOf("sapling_item").forGetter(SeedSaplingRecipe::getSaplingItem)
            ).apply(instance, (saplingBlock, saplingItem) -> new SeedSaplingRecipe(
                    saplingBlock.orElse(null),
                    saplingItem.orElseGet(() ->
                            saplingBlock.orElseThrow(() -> new RuntimeException("Seed-Sapling recipe requires " +
                                            "either a block or item, at least.")).asItem()
                    )
            ))
    );

    private final List<Item> extraIngredientsForSaplingToSeed = new LinkedList<>();
    private final List<Item> extraIngredientsForSeedToSapling = new LinkedList<>();
    private Block saplingBlock;
    private Item saplingItem;
    private boolean canCraftSaplingToSeed = true;
    private boolean canCraftSeedToSapling = true;

    public SeedSaplingRecipe(@Nullable Block saplingBlock, Item saplingItem) {
        this(saplingItem);
        this.saplingBlock = saplingBlock;
    }

    public SeedSaplingRecipe(Item saplingItem) {
        setSaplingItem(saplingItem);
    }

    public void addExtraIngredientForSaplingToSeed(Item ingredient) {
        extraIngredientsForSaplingToSeed.add(ingredient);
    }

    public void addExtraIngredientForSeedToSapling(Item ingredient) {
        extraIngredientsForSeedToSapling.add(ingredient);
    }

    public void setCanCraftSaplingToSeed(boolean canCraftSaplingToSeed) {
        this.canCraftSaplingToSeed = canCraftSaplingToSeed;
    }

    public void setCanCraftSeedToSapling(boolean canCraftSeedToSapling) {
        this.canCraftSeedToSapling = canCraftSeedToSapling;
    }

    public Optional<Item> getSaplingItem() {
        return Optionals.ofItem(this.saplingItem);
    }

    public SeedSaplingRecipe setSaplingItem(Item saplingItem) {
        this.saplingItem = saplingItem;
        return this;
    }

    public boolean isValid() {
        return this.getSaplingItem().isPresent();
    }

    @Nonnull
    public Optional<Block> getSaplingBlock() {
        return Optionals.ofBlock(saplingBlock);
    }

    @Nonnull
    public List<Item> getIngredientsForSaplingToSeed() {
        return new LinkedList<>(extraIngredientsForSaplingToSeed);
    }

    @Nonnull
    public List<Item> getIngredientsForSeedToSapling() {
        return new LinkedList<>(extraIngredientsForSeedToSapling);
    }

    public boolean canCraftSaplingToSeed() {
        return canCraftSaplingToSeed;
    }

    public boolean canCraftSeedToSapling() {
        return canCraftSeedToSapling;
    }

}
