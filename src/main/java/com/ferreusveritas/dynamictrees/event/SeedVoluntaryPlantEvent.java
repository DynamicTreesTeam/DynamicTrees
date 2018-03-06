package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class SeedVoluntaryPlantEvent extends Event  {

	EntityItem seedEntityItem;
	ItemStack seedStack;
	Species species;
	BlockPos pos;//Where the sapling will be created
	boolean forcePlant = false;
	
	public SeedVoluntaryPlantEvent(EntityItem entityItem, Species species, BlockPos pos) {
		this.seedEntityItem = entityItem;
		this.pos = pos;
		this.species = species;
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public EntityItem getEntityItem() {
		return seedEntityItem;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	/**
	 * Use this to force the seed to plant regardless of it's natural chances
	 *  
	 * @param force true to force sapling to plant.  false will allow nature to take it's coarse.
	 */
	public void forcePlanting(boolean force) {
		this.forcePlant = force;
	}
	
	public boolean doForcePlant() {
		return forcePlant;
	}
}
