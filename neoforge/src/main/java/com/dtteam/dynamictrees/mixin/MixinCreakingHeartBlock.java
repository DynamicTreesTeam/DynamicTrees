package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreakingHeartBlock.class)
public class MixinCreakingHeartBlock {

    @Inject(method = "hasRequiredLogs", at = @At("HEAD"), cancellable = true)
    private static void hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CreakingHeartBranchBlock){
            cir.setReturnValue(CreakingHeartBranchBlock.hasRequiredLogs(state, level, pos));
        }
    }

}
