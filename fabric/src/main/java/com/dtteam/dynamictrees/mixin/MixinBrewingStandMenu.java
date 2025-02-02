package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.item.DendroPotion;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandMenu.PotionSlot.class)
public class MixinBrewingStandMenu {

    @Inject(at = @At("HEAD"), method = "mayPlaceItem", cancellable = true)
    private static void mayPlaceItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof DendroPotion){
            cir.setReturnValue(true);
        }
    }

}
