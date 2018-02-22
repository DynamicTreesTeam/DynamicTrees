package com.ferreusveritas.dynamictrees.event;

import java.util.List;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SeedVoluntaryDropEvent extends Event {

        private final BlockPos rootPos;
        private final Species species;
        private final List<ItemStack> dropList;
        
        public SeedVoluntaryDropEvent(BlockPos rootPos, Species species, List<ItemStack> dropList) {
            this.rootPos = rootPos;
            this.species = species;
            this.dropList = dropList;
        }

        public BlockPos getRootPos() {
			return rootPos;
		}
	
        public Species getSpecies() {
			return species;
		}
        
        public List<ItemStack> getDropList() {
			return dropList;
		}
        
}
