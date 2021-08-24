package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SeedSaplingRecipe {

    private Block saplingBlock;
    private Item saplingItem;

    private final List<Item> extraIngredientsForSaplingToSeed = new LinkedList<>();
    private final List<Item> extraIngredientsForSeedToSapling = new LinkedList<>();

    private boolean canCraftSaplingToSeed = true;
    private boolean canCraftSeedToSapling = true;

    public SeedSaplingRecipe (@Nullable Block saplingBlock, Item saplingItem){
        this(saplingItem);
        this.saplingBlock = saplingBlock;
    }
    public SeedSaplingRecipe (Item saplingItem){
        setSaplingItem(saplingItem);
    }
    public SeedSaplingRecipe setSaplingItem (Item saplingItem){
        this.saplingItem = saplingItem;
        return this;
    }

    public void addExtraIngredientForSaplingToSeed (Item ingredient){
        extraIngredientsForSaplingToSeed.add(ingredient);
    }
    public void addExtraIngredientForSeedToSapling (Item ingredient){
        extraIngredientsForSeedToSapling.add(ingredient);
    }

    public void setCanCraftSaplingToSeed(boolean canCraftSaplingToSeed){
        this.canCraftSaplingToSeed = canCraftSaplingToSeed;
    }
    public void setCanCraftSeedToSapling(boolean canCraftSeedToSapling){
        this.canCraftSeedToSapling = canCraftSeedToSapling;
    }

    public Item getSaplingItem (){
        if (saplingItem == null) return Items.AIR;
        return saplingItem;
    }

    public boolean isValid (){
        return getSaplingItem() != Items.AIR;
    }

    @Nonnull
    public Optional<Block> getSaplingBlock (){
        return Optional.ofNullable(saplingBlock);
    }
    @Nonnull
    public List<Item> getIngredientsForSaplingToSeed(){
        return new LinkedList<>(extraIngredientsForSaplingToSeed);
    }
    @Nonnull
    public List<Item> getIngredientsForSeedToSapling(){
        return new LinkedList<>(extraIngredientsForSeedToSapling);
    }

    public boolean canCraftSaplingToSeed (){
        return canCraftSaplingToSeed;
    }
    public boolean canCraftSeedToSapling (){
        return canCraftSeedToSapling;
    }

}
