package com.ferreusveritas.dynamictrees.tileentity;

import javax.annotation.Nonnull;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * 
 * A TileEntity that holds a species value.
 * 
 * @author ferreusveritas
 *
 */
public class SpeciesTileEntity extends TileEntity {
	
	private Species species = Species.NULLSPECIES;
	
	public SpeciesTileEntity() {
		super(DTRegistries.speciesTE);
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
		this.markDirty();
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		if(tag.contains("species")) {
			ResourceLocation speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
		super.read(state, tag);
	}

	@Nonnull
	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putString("species", species.getRegistryName().toString());
		return super.write(tag);
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
		return null;
	}
}
