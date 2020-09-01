package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * 
 * A TileEntity that holds a species value.
 * 
 * @author ferreusveritas
 *
 */
public class TileEntityBonsai extends TileEntity {
	
	Species species = Species.NULLSPECIES;
	BlockState potState = Blocks.FLOWER_POT.getDefaultState();
	ResourceLocation speciesName = species.getRegistryName();

	/** CHANGE THIS */
	public TileEntityBonsai() {
		super(TileEntityType.BARREL);
	}

//	public TileEntityBonsai(TileEntityType<?> tileEntityTypeIn) {
//		super(TileEntityType.Builder.create(new Supplier<TileEntityBonsai>() {
//			@Override
//			public TileEntityBonsai get() {
//				return null;
//			}
//		}, DTRegistries.blockBonsaiPot).build());
//	}

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
	
	public BlockState getPot() {
		return potState;
	}
	
	public void setPot(BlockState newPotState) {
		if(newPotState.getBlock() instanceof FlowerPotBlock) {
			this.potState = newPotState.getBlock().getDefaultState();
		} else {
			this.potState = Blocks.FLOWER_POT.getDefaultState();
		}
		this.markDirty();
	}

	@Override
	public void read(CompoundNBT tag) {
		if(tag.contains("species")) {
			speciesName = new ResourceLocation(tag.getString("species"));
			species = TreeRegistry.findSpecies(speciesName);
		}
		if(tag.contains("pot")) {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("pot")));
			potState = block != Blocks.AIR ? block.getDefaultState() : Blocks.FLOWER_POT.getDefaultState();
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putString("species", speciesName.toString());
		tag.putString("pot", potState.getBlock().getRegistryName().toString());
		return tag;
	}
	
//	@Override
//	public void readFromNBT(CompoundNBT tag) {
//		super.readFromNBT(tag);
//		read(tag);
//	}
//
//	@Override
//	public CompoundNBT writeToNBT(CompoundNBT tag) {
//		super.writeToNBT(tag);
//		write(tag);
//
//		return tag;
//	}
//
//	@Override
//	public SPacketUpdateTileEntity getUpdatePacket() {
//		CompoundNBT syncData = new CompoundNBT();
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
//	public CompoundNBT getUpdateTag() {
//		return this.writeToNBT(new CompoundNBT());
//	}
	
}
