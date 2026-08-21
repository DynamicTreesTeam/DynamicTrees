package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class MegaSeedRecipe extends CustomRecipe {

    public MegaSeedRecipe() {
        super();
    }

    public boolean matches(CraftingInput craftingInput, Level level) {
        if(DTConfigs.COMMON.generateMegaSeedRecipe.get() && atLeastHasSeed(craftingInput)){
            for(Species species : Species.REGISTRY) {
                if(recipeMatchCondition(craftingInput, species)) {
                    return nonEmptyStacksStream(craftingInput).count() == 4;
                }
            }
        }
        return false;
    }

    public ItemStack assemble(CraftingInput craftingInput) {
        for (Species species : Species.REGISTRY) {
            if (recipeMatchCondition(craftingInput, species)) {
                return new ItemStack(species.getSeed().get());
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean atLeastHasSeed(CraftingInput craftingInput) {
        return craftingInput.items().stream().anyMatch(s -> !s.isEmpty() && s.getItem() instanceof Seed);
    }

    private static boolean recipeMatchCondition(CraftingInput craftingInput, Species species) {
        return species.isMegaSpecies() && species.hasSeed()
                && species.getPreMegaSpecies().canCraftMegaSeed()
                && allItemsMatchSeed(craftingInput, species.getPreMegaSpecies());
    }

    private static boolean allItemsMatchSeed(CraftingInput craftingInput, Species species) {
        return nonEmptyStacksStream(craftingInput).allMatch(stack -> stack.is(species.getSeed().get()));
    }

    private static Stream<ItemStack> nonEmptyStacksStream(CraftingInput craftingInput) {
        return craftingInput.items().stream().filter(stack -> !stack.isEmpty());
    }

    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return DTRegistries.MEGA_SEED_RECIPE_TYPE.get();
    }

}