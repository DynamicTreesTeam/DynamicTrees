package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.recipe.DendroBrewingMix;
import com.dtteam.dynamictrees.recipe.DendroPotionRecipeHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class MixinPotionBrewing {

    @Inject(at = @At("HEAD"), method = "isIngredient", cancellable = true)
    private void isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (DendroPotionRecipeHandler.isIngredient(stack)){
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "isContainer", cancellable = true)
    private void isContainer(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof DendroPotion){ //Add the dendro potion as a valid container
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "hasMix", cancellable = true)
    private void hasMix(ItemStack potionBase, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        for (DendroBrewingMix mix : DendroPotionRecipeHandler.getAllDendroRecipes()){
            if (mix.isInput(potionBase) && mix.isIngredient(ingredient)){
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mix", cancellable = true)
    private void mix(ItemStack ingredient, ItemStack potionBase, CallbackInfoReturnable<ItemStack> cir) {
        for (DendroBrewingMix mix : DendroPotionRecipeHandler.getAllDendroRecipes()){
            ItemStack result = mix.getOutput(potionBase, ingredient);
            if (!result.isEmpty()){
                cir.setReturnValue(result);
                return;
            }
        }
    }

}
