package com.dtteam.dynamictrees.block.sapling;

import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class PottedSaplingBlockEntity extends BlockEntity {

    private static final String POT_MIMIC_TAG = "pot_mimic";
    private static final String SPECIES_TAG = "species";

    protected BlockState potState = Blocks.FLOWER_POT.defaultBlockState();
    protected Species species = Species.NULL_SPECIES;

    public PottedSaplingBlockEntity(BlockPos pos, BlockState state) {
        super(DTRegistries.POTTED_SAPLING_BLOCK_ENTITY.get(), pos, state); //
    }

    public Species getSpecies() {
        return this.species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    public BlockState getPot() {
        return potState;
    }

    public void setPot(BlockState newPotState) {
        if (newPotState.getBlock() instanceof FlowerPotBlock) {
            this.potState = newPotState.getBlock().defaultBlockState();
        } else {
            this.potState = Blocks.FLOWER_POT.defaultBlockState();
        }
        this.setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        // saveAdditional now uses ValueOutput
        return tag;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
    }

    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
    }

}
