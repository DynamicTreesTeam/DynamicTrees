package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.world.World;

public interface IBiomeSuitabilityDecider {

	public Decision getBiomeSuitability(World world, DynamicTree tree, BlockPos pos);
	
	public class Decision {
		private boolean handled;
		private float suitability;
		
		public Decision() {
			handled = false;
		}
		
		/**
		* @return suitability range from 0.0 - 1.0.  (0.0f for completely unsuited.. 1.0f for perfectly suited)
		*/
		public Decision(float suitability) {
			this.suitability = suitability;
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
