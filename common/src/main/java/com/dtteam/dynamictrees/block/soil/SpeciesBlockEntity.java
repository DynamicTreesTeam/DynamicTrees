package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class SpeciesBlockEntity extends BlockEntity {

    private Species species = Species.NULL_SPECIES;

    public SpeciesBlockEntity(BlockPos pos, BlockState state) {
        super(DTRegistries.SPECIES_BLOCK_ENTITY.get(), pos, state);
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("species")) {
            ResourceLocation speciesName = ResourceLocation.parse(tag.getString("species"));
            species = Species.findSpecies(speciesName);
        }
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("species", species.getRegistryName().toString());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    //    @Override
//    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
//        load(packet.getTag());
//    }
//

}
