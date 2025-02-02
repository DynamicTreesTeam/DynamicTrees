package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.item.DendroPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class MixinBrewingStandBlockEntity {

    @Inject(at = @At("HEAD"), method = "canPlaceItem", cancellable = true)
    private void canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot != 3 && slot != 4 && stack.getItem() instanceof DendroPotion){
            cir.setReturnValue(true);
        }
    }

}
