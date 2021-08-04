package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * A tile entity for the {@link BlockBonsaiPot} that holds a {@link Species} value.
 * 
 * @author ferreusveritas
 */
public class TileEntityBonsai extends TileEntity {
	
	Species species = Species.NULLSPECIES;
	IBlockState potState = Blocks.FLOWER_POT.getDefaultState();
	ResourceLocation speciesName = species.getRegistryName();
	
	public Species getSpecies() {
		if (species == Species.NULLSPECIES) {
			species = TreeRegistry.findSpecies(speciesName);
		}
		return species;
	}
	
	public void setSpecies(Species species) {
		this.species = species;
		this.speciesName = species.getRegistryName();
		this.markDirty();
	}
	
	public IBlockState getPot() {
		return potState;
	}
	
	public void setPot(IBlockState newPotState) {
		if(newPotState.getBlock() instanceof BlockFlowerPot) {
			this.potState = newPotState.getBlock().getDefaultState();
		} else {
			this.potState = Blocks.FLOWER_POT.getDefaultState();
		}
		this.markDirty();
	}
	
	private void read(NBTTagCompound tag) {
		if(tag.hasKey("species")) {
			speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
		if(tag.hasKey("pot")) {
			Block block = Block.REGISTRY.getObject(new ResourceLocation(tag.getString("pot")));
			potState = block != Blocks.AIR ? block.getDefaultState() : Blocks.FLOWER_POT.getDefaultState();
		}
	}
	
	private void write(NBTTagCompound tag) {
		tag.setString("species", speciesName.toString());
		tag.setString("pot", potState.getBlock().getRegistryName().toString());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		read(tag);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		write(tag);
		
		return tag;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound syncData = new NBTTagCompound();
		this.write(syncData);
		return new SPacketUpdateTileEntity(this.pos, 1, syncData);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		read(pkt.getNbtCompound());
	}
	
	//Packages up the data on the server to send to the client.  Client handles it with handleUpdateTag() which reads it with readFromNBT()
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
}
