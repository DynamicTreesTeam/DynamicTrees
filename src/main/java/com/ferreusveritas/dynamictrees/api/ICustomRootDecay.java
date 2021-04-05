package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;

/**
 * Implementations of this {@code interface} allow for custom logic when decaying
 * {@link RootyBlock}s after a tree has fallen.
 *
 * <p>The implementation should be registered via
 * {@link TreeHelper#setCustomRootBlockDecay(ICustomRootDecay)}.</p>
 *
 * @author ferreusveritas
 */
public interface ICustomRootDecay {

    /**
     * Implementations perform their custom {@link RootyBlock} decay logic.
     *
     * @param world The {@link World} instance.
     * @param rootPos The {@link BlockPos} of the {@link RootyBlock}.
     * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
     * @param species The {@link Species} of the tree that was removed.
     * @return {@code true} if handled; otherwise {@code false} to run
     *         the default decay algorithm.
     */
    boolean doDecay(World world, BlockPos rootPos, BlockState rootyState, Species species);

}
