package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

/**
 * {@link GenFeature} implementation can implement this to generate after a branch has rotted.
 *
 * @author Harley O'Connor
 */
public interface IPostRotGenFeature {

    /**
     * Executed from {@link Species#rot(IWorld, BlockPos, int, int, int, Random, boolean, boolean)}, allowing actions
     * after the tree has rotted.
     *
     * @param configuredGenFeature The {@link ConfiguredGenFeature} generating this.
     * @param world                The {@link IWorld} object.
     * @param pos                  The {@link BlockPos} of the rot.
     * @param neighborCount        The number of neighbours.
     * @param radius               The radius of the branch.
     * @param fertility            The fertility of the tree.
     * @param random               A {@link Random} object.
     * @param rapid                True if this postRot is happening under a generation scenario as opposed to natural
     *                             tree updates.
     */
    void postRot(final ConfiguredGenFeature<?> configuredGenFeature, final IWorld world, final BlockPos pos, final int neighborCount, final int radius, final int fertility, final Random random, final boolean rapid);

}
