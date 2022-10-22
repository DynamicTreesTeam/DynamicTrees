package com.ferreusveritas.dynamictrees.api;

import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Implementations of this {@code interface} allow for custom logic when decaying {@link RootyBlock}s after a tree has
 * fallen.
 *
 * <p>The implementation should be registered via
 * {@link TreeHelper#setCustomRootBlockDecay(RootyBlockDecayer)}.</p>
 *
 * @author ferreusveritas
 */
@FunctionalInterface
public interface RootyBlockDecayer {

    /**
     * Implementations perform their custom {@link RootyBlock} decay logic.
     *
     * @param level      The {@link Level} instance.
     * @param rootPos    The {@link BlockPos} of the {@link RootyBlock}.
     * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
     * @param species    The {@link Species} of the tree that was removed.
     * @return {@code true} if handled; otherwise {@code false} to run the default decay algorithm.
     */
    boolean decay(Level level, BlockPos rootPos, BlockState rootyState, Species species);

}
