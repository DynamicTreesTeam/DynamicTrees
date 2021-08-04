package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IPreGenFeature {

    /**
     * Do pre generation operations
     *
     * @param configuredGenFeature The {@link ConfiguredGenFeature}
     * @param world                The world
     * @param rootPos              The position of the rooty dirt
     * @param species              The species being processed
     * @param radius               The Poisson disc radius
     * @param facing               The facing direction that will be applied to the JoCode during generation
     * @param safeBounds           A safebounds structure for preventing runaway cascading generation
     * @param joCode               The JoCode that will be used to generate this tree
     * @return The modified position of the rooty dirt
     */
    BlockPos preGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, int radius, Direction facing, SafeChunkBounds safeBounds, JoCode joCode);

}
