package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.registry.PendingRegistryIds;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockItem.class)
public class MixinBlockItem {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static Item.Properties dynamictrees$useBlockDescriptionPrefix(Item.Properties properties) {
        if (PendingRegistryIds.ITEM.get() != null) {
            properties.useBlockDescriptionPrefix();
        }
        return properties;
    }
}
