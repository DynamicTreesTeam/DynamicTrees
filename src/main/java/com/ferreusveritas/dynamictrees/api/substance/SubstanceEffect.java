package com.ferreusveritas.dynamictrees.api.substance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * A substance effect is like a potion effect but for trees.
 *
 * @author ferreusveritas
 */
public interface SubstanceEffect {

    /**
     * For an instant effect.
     *
     * @return true for success.  false otherwise
     */
    boolean apply(Level level, BlockPos rootPos);

    /**
     * For a continuously updating effect.
     *
     * @return true to stay alive. false to kill effector
     */
    default boolean update(Level level, BlockPos rootPos, int deltaTicks, int fertility) {
        return false;
    }

    /**
     * Get the name of the effect.  Used to compare existing effects in the environment.
     *
     * @return the name of the effect.
     */
    String getName();

    /**
     * Determines if the effect is continuous or instant
     *
     * @return true if continuous, false if instant
     */
    boolean isLingering();

}
