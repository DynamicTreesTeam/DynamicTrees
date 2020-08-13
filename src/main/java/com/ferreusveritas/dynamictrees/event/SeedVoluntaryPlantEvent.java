package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SeedVoluntaryPlantEvent extends Event {

	protected ItemEntity seedEntityItem;
	protected ItemStack seedStack;
	protected Species species;
	protected BlockPos pos;//Where the sapling will be created
	protected boolean willPlant = false;
	
	public SeedVoluntaryPlantEvent(ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant) {
		this.seedEntityItem = entityItem;
		this.pos = pos;
		this.species = species;
		this.willPlant = willPlant;
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
	}
	
	public ItemEntity getEntityItem() {
		return seedEntityItem;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	/**
	 * Use this to force the seed to plant regardless of it's natural chances
	 *  
	 * @param willPlant true to force sapling to plant.  false will allow nature to take it's coarse.
	 */
	public void setWillPlant(boolean willPlant) {
		this.willPlant = willPlant;
	}
	
	public boolean getWillPlant() {
		return willPlant;
	}
}
