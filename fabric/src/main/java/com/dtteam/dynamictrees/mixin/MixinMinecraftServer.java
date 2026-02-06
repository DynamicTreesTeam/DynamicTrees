package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.platform.FabricMiscHelper;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, priority = 500)
public abstract class MixinMinecraftServer {
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onServerConstruction(CallbackInfo ci) {
        FabricMiscHelper.currentServer = (MinecraftServer) (Object) this;
    }
}
