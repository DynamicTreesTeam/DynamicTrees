package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
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

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(FungusFamily::new);

    public FungusFamily (ResourceLocation name) {
        super(name);
    }

    @Override
    public SoundType getBranchSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return SoundType.STEM;
    }

    @Override
    public boolean isFireProof() { return true; }

}
