package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.tree.species.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;

public class MegaSeedRecipe extends CustomRecipe {
    public MegaSeedRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (DTConfigs.COMMON.generateMegaSeedRecipe.get()) {
//            if(craftingInput.items().stream().anyMatch(stack->stack.is(DTRegistries.DIRT_BUCKET.get()))) {
            for (Species species : Species.REGISTRY) {
                if (species.isMegaSpecies()) {
                    if (craftingInput.items().stream().filter(stack -> stack.is(species.getPreMegaSpecies().getSeed().get())).count() == 4) {
                        return craftingInput.items().stream().filter(stack -> !stack.isEmpty()).count() == 4;
                    }
                }
            }
//          }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        for (Species species : Species.REGISTRY) {
            if (species.isMegaSpecies()) {
                if (craftingInput.items().stream().filter(stack -> stack.is(species.getPreMegaSpecies().getSeed().get())).count() == 4) {
                    return new ItemStack(species.getSeed().get());
                }
            }
        }
        return ItemStack.EMPTY;
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
