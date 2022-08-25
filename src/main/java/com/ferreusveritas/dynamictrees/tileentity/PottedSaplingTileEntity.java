package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A TileEntity that holds a species value.
 *
 * @author ferreusveritas
 */
public class PottedSaplingTileEntity extends BlockEntity {

    private static final String POT_MIMIC_TAG = "pot_mimic";
    private static final String SPECIES_TAG = "species";

    public static final ModelProperty<BlockState> POT_MIMIC = new ModelProperty<>();
    public static final ModelProperty<Species> SPECIES = new ModelProperty<>();

    private BlockState potState = Blocks.FLOWER_POT.defaultBlockState();
    private Species species = Species.NULL_SPECIES;

    public PottedSaplingTileEntity(BlockPos pos, BlockState state) {
        super(DTRegistries.bonsaiTE,pos,state);
    }

    public Species getSpecies() {
        return this.species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
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
        level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldPotState = potState;
        this.handleUpdateTag(pkt.getTag());

        if (!oldPotState.equals(potState)) {
            level.getModelDataManager().requestRefresh(this);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains(POT_MIMIC_TAG)) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString(POT_MIMIC_TAG)));
            potState = block != Blocks.AIR ? block.defaultBlockState() : Blocks.FLOWER_POT.defaultBlockState();
        }
        if (tag.contains(SPECIES_TAG)) {
            this.species = TreeRegistry.findSpecies(tag.getString(SPECIES_TAG));
        }
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString(POT_MIMIC_TAG, ForgeRegistries.BLOCKS.getKey(potState.getBlock()).toString());
        tag.putString(SPECIES_TAG, this.species.getRegistryName().toString());
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(POT_MIMIC, potState).with(SPECIES, species).build();
    }

}
