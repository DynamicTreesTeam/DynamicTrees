package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 
 * A TileEntity that holds a species value.
 * 
 * @author ferreusveritas
 *
 */
public class BonsaiTileEntity extends TileEntity {

	private static final String POT_MIMIC_TAG = "pot_mimic";
	private static final String SPECIES_TAG = "species";

	public static final ModelProperty<BlockState> POT_MIMIC = new ModelProperty<>();
	public static final ModelProperty<Species> SPECIES = new ModelProperty<>();

	private BlockState potState = Blocks.FLOWER_POT.getDefaultState();
	private Species species = Species.NULL_SPECIES;

	public BonsaiTileEntity() {
		super(DTRegistries.bonsaiTE);
	}

	public Species getSpecies() {
		return this.species;
	}

	public void setSpecies(Species species) {
		this.species = species;
		this.markDirty();
		world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
		world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		this.write(tag);
		return tag;
	}

	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, 1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		BlockState oldPotState = potState;
		this.handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());

		if (!oldPotState.equals(potState)) {
			ModelDataManager.requestModelDataRefresh(this);
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
		}
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		if(tag.contains(POT_MIMIC_TAG)) {
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString(POT_MIMIC_TAG)));
			potState = block != Blocks.AIR ? block.getDefaultState() : Blocks.FLOWER_POT.getDefaultState();
		}
		if (tag.contains(SPECIES_TAG)) {
			this.species = TreeRegistry.findSpecies(tag.getString(SPECIES_TAG));
		}
		super.read(state, tag);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.putString(POT_MIMIC_TAG, potState.getBlock().getRegistryName().toString());
		tag.putString(SPECIES_TAG, this.species.getRegistryName().toString());
		return super.write(tag);
	}

	@Nonnull
	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(POT_MIMIC, potState).withInitial(SPECIES, species).build();
	}

}
