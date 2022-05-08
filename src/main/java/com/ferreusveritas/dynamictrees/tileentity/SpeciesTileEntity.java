package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class SpeciesTileEntity extends BlockEntity {

    private Species species = Species.NULL_SPECIES;

    public SpeciesTileEntity(BlockPos pos, BlockState state) {
        super(DTRegistries.speciesTE, pos, state);
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("species")) {
            ResourceLocation speciesName = new ResourceLocation(tag.getString("species"));
            species = TreeRegistry.findSpecies(speciesName);
        }
        super.load( tag);
    }

    @Nonnull
    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putString("species", species.getRegistryName().toString());
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

}
