package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.registry.PendingRegistryIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class MixinItem {

    @Inject(method = "<init>(Lnet/minecraft/world/item/Item$Properties;)V", at = @At("HEAD"))
    private static void dynamictrees$setItemId(Item.Properties properties, CallbackInfo ci) {
        Identifier id = PendingRegistryIds.ITEM.get();
        if (id != null) {
            properties.setId(ResourceKey.create(Registries.ITEM, id));
        }
    }
}
