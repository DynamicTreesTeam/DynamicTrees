//package com.dtteam.dynamictrees.compat;
//
//import com.dtteam.dynamictrees.DynamicTrees;
//import com.dtteam.dynamictrees.item.DendroPotion;
//import com.dtteam.dynamictrees.recipe.DendroPotionRecipeHandler;
//import com.dtteam.dynamictrees.registry.DTRegistries;
//import mezz.jei.api.IModPlugin;
//import mezz.jei.api.JeiPlugin;
//import mezz.jei.api.constants.RecipeTypes;
//import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
//import mezz.jei.api.ingredients.subtypes.UidContext;
//import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
//import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
//import mezz.jei.api.registration.IRecipeRegistration;
//import mezz.jei.api.registration.ISubtypeRegistration;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.item.ItemStack;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @author Harley O'Connor
// */
//@JeiPlugin
//public final class DTJeiPlugin implements IModPlugin {
//
//    @Override
//    public Identifier getPluginUid() {
//        return DynamicTrees.location(DynamicTrees.MOD_ID);
//    }
//
//    static final class DendroSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
//
//        @Override
//        public @Nullable Object getSubtypeData(ItemStack itemStack, UidContext uidContext) {
//            if (itemStack.getItem() instanceof DendroPotion){
//                return DendroPotion.getPotionType(itemStack);
//            }
//            return null;
//        }
//
////        @Override
////        public String getLegacyStringSubtypeInfo(ItemStack itemStack, UidContext uidContext) {
////            if (itemStack.getItem() instanceof DendroPotion){
////                return DendroPotion.getPotionType(itemStack).getName();
////            }
////            return "";
////        }
//    }
//
//    @Override
//    public void registerItemSubtypes(final ISubtypeRegistration registration) {
//        registration.registerSubtypeInterpreter(DTRegistries.DENDRO_POTION.get(), new DendroSubtypeInterpreter());
//    }
//
//    @Override
//    public void registerRecipes(final IRecipeRegistration registration) {
//        final IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
//        final List<IJeiBrewingRecipe> brewingRecipes = new ArrayList<>();
//
//        DendroPotionRecipeHandler.getAllDendroRecipes().forEach(recipe ->
//                brewingRecipes.add(makeJeiBrewingRecipe(factory, recipe.input(), recipe.ingredient(), recipe.output())));
//
//        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
//    }
//
//    private static IJeiBrewingRecipe makeJeiBrewingRecipe(IVanillaRecipeFactory factory, final ItemStack inputStack, final ItemStack ingredientStack, ItemStack output) {
//        return factory.createBrewingRecipe(Collections.singletonList(ingredientStack), inputStack, output, Identifier.parse(""));
//    }
//
//}
