package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class SpeciesBlockEntity extends BlockEntity {

    private Species species = Species.NULL_SPECIES;

    public SpeciesBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state); //DTRegistries.SPECIES_BLOCK_ENTITY
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
    }

//    @Override
//    public void load(CompoundTag tag) {
//        if (tag.contains("species")) {
//            ResourceLocation speciesName = ResourceLocation.parse(tag.getString("species"));
//            species = TreeRegistry.findSpecies(speciesName);
//        }
//        super.load(tag);
//    }
//
//    @Nonnull
//    @Override
//    public void saveAdditional(CompoundTag tag) {
//        tag.putString("species", species.getRegistryName().toString());
//    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

//    @Override
//    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
//        load(packet.getTag());
//    }
//
//    @Override
//    public CompoundTag getUpdateTag() {
//        CompoundTag tag = super.getUpdateTag();
//        this.saveAdditional(tag);
//        return tag;
//    }

}
