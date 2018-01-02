package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * A TileEntity that holds a species value.
 * 
 * @author ferreusveritas
 *
 */
public class TileEntitySpecies extends TileEntity {

	Species species = Species.NULLSPECIES;
	ResourceLocation speciesName;
	
	public Species getSpecies() {
		if(species == Species.NULLSPECIES) {
			species = TreeRegistry.findSpecies(speciesName);
		}
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
		this.speciesName = species.getRegistryName();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if(tag.hasKey("species")) {
			speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setString("species", speciesName.toString());
		return tag;
	}
}
