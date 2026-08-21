package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelExtractor.class)
public class MixinLevelExtractor {

    @Shadow
    private ClientLevel level;

    @Inject(method = "extractBlockDestroyAnimation", at = @At("TAIL"))
    private void dynamictrees$remapTrunkShellBreaking(Camera camera, LevelRenderState renderState, CallbackInfo ci) {
        if (level == null) {
            return;
        }
        List<BlockBreakingRenderState> states = renderState.blockBreakingRenderStates;
        for (int i = 0; i < states.size(); i++) {
            BlockBreakingRenderState breaking = states.get(i);
            if (!(breaking.blockState().getBlock() instanceof TrunkShellBlock shell)) {
                continue;
            }
            TrunkShellBlock.ShellMuse muse = shell.getMuseUnchecked(level, breaking.blockState(), breaking.blockPos());
            if (muse != null && muse.getRadius() > 8) {
                states.set(i, new BlockBreakingRenderState(muse.pos(), muse.state(), breaking.progress()));
            }
        }
    }
}
