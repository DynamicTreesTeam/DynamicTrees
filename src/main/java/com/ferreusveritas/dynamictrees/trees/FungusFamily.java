package com.ferreusveritas.dynamictrees.trees;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class FungusFamily extends Family {

    public FungusFamily (ResourceLocation name) {
        super(name);
    }

    @Override
    public SoundType getBranchSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return SoundType.HYPHAE;
    }

    @Override
    public boolean isFireProof() { return true; }

    public static class Type extends Family.Type {
        @Override
        public FungusFamily construct(ResourceLocation registryName) {
            return new FungusFamily(registryName);
        }
    }

}
