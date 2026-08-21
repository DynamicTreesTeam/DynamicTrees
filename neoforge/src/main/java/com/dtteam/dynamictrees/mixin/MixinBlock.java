package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.registry.PendingRegistryIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("HEAD"))
    private static void dynamictrees$setBlockId(BlockBehaviour.Properties properties, CallbackInfo ci) {
        Identifier id = PendingRegistryIds.BLOCK.get();
        if (id != null) {
            properties.setId(ResourceKey.create(Registries.BLOCK, id));
        }
    }
}
