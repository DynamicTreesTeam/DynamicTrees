package com.ferreusveritas.dynamictrees.recipe;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.item.Seed;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.stream.Stream;

public class MegaSeedRecipe extends CustomRecipe {

    public MegaSeedRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer craftingInput, Level level) {
        if(DTConfigs.GENERATE_MEGA_SEED_RECIPE.get() && atLeastHasSeed(craftingInput)){
            for(Species species : Species.REGISTRY) {
                if(recipeMatchCondition(craftingInput, species)) {
                    return nonEmptyStacksStream(craftingInput).count() == 4;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInput, RegistryAccess registryAccess) {
        for (Species species : Species.REGISTRY) {
            if (recipeMatchCondition(craftingInput, species)) {
                return new ItemStack(species.getSeed().get());
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean atLeastHasSeed(CraftingContainer craftingInput) {
        return craftingInput.getItems().stream().anyMatch(s -> !s.isEmpty() && s.getItem() instanceof Seed);
    }

    private static boolean recipeMatchCondition(CraftingContainer craftingInput, Species species) {
        return species.isMegaSpecies() && species.hasSeed()
                && species.getPreMegaSpecies().canCraftMegaSeed()
                && allItemsMatchSeed(craftingInput, species.getPreMegaSpecies());
    }

    private static boolean allItemsMatchSeed(CraftingContainer craftingInput, Species species) {
        return nonEmptyStacksStream(craftingInput).allMatch(stack -> stack.is(species.getSeed().get()));
    }

    private static Stream<ItemStack> nonEmptyStacksStream(CraftingContainer craftingInput) {
        return craftingInput.getItems().stream().filter(stack -> !stack.isEmpty());
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DTRegistries.MEGA_SEED_RECIPE_TYPE.get();
    }
}
