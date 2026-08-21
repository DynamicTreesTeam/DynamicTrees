package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.entity.BlockEntityTypeRebind;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntityTypeRebind implements BlockEntityTypeRebind {

    @Shadow
    @Final
    @Mutable
    private BlockEntityType<?> type;

    @Override
    public void dynamictrees$rebindType(BlockEntityType<?> type) {
        this.type = type;
    }

}
