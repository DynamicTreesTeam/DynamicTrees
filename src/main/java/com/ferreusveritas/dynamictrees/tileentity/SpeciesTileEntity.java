package com.ferreusveritas.dynamictrees.tileentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * A TileEntity that holds a species value.
 * 
 * @author ferreusveritas
 *
 */
public class SpeciesTileEntity extends TileEntity {
	
	private Species species = Species.NULL_SPECIES;
	
	public SpeciesTileEntity() {
		super(DTRegistries.speciesTE);
	}
	
	public Species getSpecies() {
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
		this.setChanged();
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		if(tag.contains("species")) {
			ResourceLocation speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
		super.load(state, tag);
	}

	@Nonnull
	@Override
	public CompoundNBT save(CompoundNBT tag) {
		tag.putString("species", species.getRegistryName().toString());
		return super.save(tag);
	}

	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		load(getBlockState(), pkt.getTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return save(new CompoundNBT());
	}

}
