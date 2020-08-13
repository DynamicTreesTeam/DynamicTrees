package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
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
	ResourceLocation speciesName = species.getRegistryName();

	public TileEntitySpecies(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public Species getSpecies() {
		if(species == Species.NULLSPECIES) {
			species = TreeRegistry.findSpecies(speciesName);
		}
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
		this.speciesName = species.getRegistryName();
		this.markDirty();
	}
	
	public void read(CompoundNBT tag) {
		if(tag.hasUniqueId("species")) {
			speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
	}
	
	public CompoundNBT write(CompoundNBT tag) {
		tag.putString("species", speciesName.toString());
		return tag;
	}
	
//	@Override
//	public void readFromNBT(NBTTagCompound tag) {
//		super.readFromNBT(tag);
//		read(tag);
//	}
//
//	@Override
//	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
//		super.writeToNBT(tag);
//		write(tag);
//
//		return tag;
//	}
//
//	@Override
//	public SPacketUpdateTileEntity getUpdatePacket() {
//		NBTTagCompound syncData = new NBTTagCompound();
//		this.write(syncData);
//		return new SPacketUpdateTileEntity(this.pos, 1, syncData);
//	}
//
//	@Override
//	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
//		read(pkt.getNbtCompound());
//	}
//
//	//Packages up the data on the server to send to the client.  Client handles it with handleUpdateTag() which reads it with readFromNBT()
//	public NBTTagCompound getUpdateTag() {
//		return this.writeToNBT(new NBTTagCompound());
//	}
//
}
