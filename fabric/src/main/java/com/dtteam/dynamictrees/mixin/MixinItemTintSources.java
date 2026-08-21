package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.client.tint.DendroPotionTintSource;
import com.dtteam.dynamictrees.client.tint.StaffTintSource;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTintSources.class)
public class MixinItemTintSources {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void dynamictrees$registerTintSources(CallbackInfo ci) {
        ID_MAPPER.put(DynamicTrees.location("dendro_potion"), DendroPotionTintSource.MAP_CODEC);
        ID_MAPPER.put(DynamicTrees.location("staff"), StaffTintSource.MAP_CODEC);
    }
}
