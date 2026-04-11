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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        if (input.getString(POT_MIMIC_TAG).isPresent()) {
            Block block = BuiltInRegistries.BLOCK.get(Identifier.parse(input.getString(POT_MIMIC_TAG).get())).get().value();
            potState = block != Blocks.AIR ? block.defaultBlockState() : Blocks.FLOWER_POT.defaultBlockState();
        }
        if (input.getString(SPECIES_TAG).isPresent()) {
            this.species = Species.findSpecies(input.getString(SPECIES_TAG).get());
        }
        super.loadAdditional(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putString(POT_MIMIC_TAG, BuiltInRegistries.BLOCK.getKey(potState.getBlock()).toString());
        output.putString(SPECIES_TAG, this.species.getRegistryName().toString());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString(POT_MIMIC_TAG, BuiltInRegistries.BLOCK.getKey(potState.getBlock()).toString());
        tag.putString(SPECIES_TAG, this.species.getRegistryName().toString());
        return tag;
    }

}
