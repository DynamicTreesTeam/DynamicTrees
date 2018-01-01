package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class TileEntityRootyDirt extends TileEntity {

	ResourceLocation speciesName;
	
	public Species getSpecies() {
		return TreeRegistry.findSpecies(speciesName);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if(compound.hasKey("species")) {
			speciesName = new ResourceLocation(compound.getString("species"));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setString("species", speciesName.toString());
		return tag;
	}
}
