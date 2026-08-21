package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.level.block.VegetationBlock.class)
public class MixinBushBlock {

    @Shadow protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos){ return false; }

    /**
     * This mixin is only done because we cannot override neoforge's canSustainPlant
     */
    @Inject(at = @At("HEAD"), cancellable = true, method = "canSurvive (Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z")
    private void canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockpos = pos.below();
        BlockState belowBlockState = level.getBlockState(blockpos);
        if (belowBlockState.getBlock() instanceof BasicRootsBlock roots){
            if (belowBlockState.getValue(BasicRootsBlock.LAYER) == BasicRootsBlock.Layer.COVERED){
                Block block = BasicRootsBlock.Layer.COVERED.getPrimitive(roots.getFamily()).orElse(null);
                if (block == null) return;
                cir.setReturnValue(mayPlaceOn(block.defaultBlockState(), level, blockpos));
            }
        }
    }

}
