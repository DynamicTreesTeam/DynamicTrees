package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class MegaSeedRecipe extends CustomRecipe {

    public static final MegaSeedRecipe INSTANCE = new MegaSeedRecipe();
    public static final RecipeSerializer<MegaSeedRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if(DTConfigs.COMMON.generateMegaSeedRecipe.get() && atLeastHasSeed(craftingInput)){
            for(Species species : Species.REGISTRY) {
                if(recipeMatchCondition(craftingInput, species)) {
                    return craftingInput.items().size() == 4;
                }
            }
        }
        return false;
    }

    @Override
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
        return craftingInput.items().stream().allMatch(stack -> stack.is(species.getSeed().get()));
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return DTRegistries.MEGA_SEED_RECIPE_TYPE.get();
    }
}
