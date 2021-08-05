package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/**
 * Provides the suitability factor for a tree at a given biome or position. Mods should implement this interface and
 * register it via the {@link TreeRegistry} to control the suitability of a tree in a {@link Biome}.
 *
 * @author ferreusveritas
 */
public interface IBiomeSuitabilityDecider {

	Decision getBiomeSuitability(World world, Biome biome, Species tree, BlockPos pos);

	/**
	 * Decision interface for handling the event
	 */
	class Decision {
		private final boolean handled;//The handling indicator
		private float suitability;//The payload

		/**
		 * Create via this constructor to leave the event unhandled so another decider can potentially handle it.
		 */
		public Decision() {
			handled = false;
		}

		/**
		 * Create via this constructor to decide what the suitability should be.
		 *
		 * @param suitability Suitability factor from 0.0 - 1.0 range.  (0.0f for completely unsuited.. 1.0f for
		 *                    perfectly suited)
		 */
		public Decision(float suitability) {
			this.suitability = MathHelper.clamp(suitability, 0f, 1f);
			handled = true;
		}

		public boolean isHandled() {
			return handled;
		}

		public float getSuitability() {
			return suitability;
		}
	}

}
