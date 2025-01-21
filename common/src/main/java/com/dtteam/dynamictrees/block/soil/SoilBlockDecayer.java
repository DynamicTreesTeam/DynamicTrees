package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.dtteam.dynamictrees.utility.helper.TreeHelper;

/**
 * Implementations of this {@code interface} allow for custom logic when decaying {@link SoilBlock}s after a tree has
 * fallen.
 *
 * <p>The implementation should be registered via
 * {@link TreeHelper#setCustomRootBlockDecay(SoilBlockDecayer)}.</p>
 *
 * @author ferreusveritas
 */
@FunctionalInterface
public interface SoilBlockDecayer {

    /**
     * Implementations perform their custom {@link SoilBlock} decay logic.
     *
     * @param level      The {@link Level} instance.
     * @param rootPos    The {@link BlockPos} of the {@link SoilBlock}.
     * @param rootyState The {@link BlockState} of the {@link SoilBlock}.
     * @param species    The {@link Species} of the tree that was removed.
     * @return {@code true} if handled; otherwise {@code false} to run the default decay algorithm.
     */
    boolean decay(Level level, BlockPos rootPos, BlockState rootyState, Species species);

}
