package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SaplingBlock.class)
public class MixinSaplingBlock {

    @Inject(method = "advanceTree", at = @At("HEAD"), cancellable = true)
    private void onAdvanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfo ci) {
        if (!Services.CONFIG.getBoolConfig(IConfigHelper.REPLACE_VANILLA_SAPLINGS)) {
            return;
        }

        Block block = state.getBlock();
        if (!DynamicSaplingBlock.SAPLING_REPLACERS.containsKey(block)) {
            return;
        }

        Species species = DynamicSaplingBlock.SAPLING_REPLACERS.get(block)
                .selfOrLocationOverride(level, pos);

        level.removeBlock(pos, false);
        ci.cancel();

        if (species.isValid()) {
            if (DynamicSaplingBlock.canSaplingStay(level, species, pos)) {
                species.transitionToTree(level, pos);
            }
        }
    }
}
