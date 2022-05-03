package com.ferreusveritas.dynamictrees.tileentity;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;
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

    public PottedSaplingTileEntity() {
        super(DTRegistries.bonsaiTE);
    }

    public Species getSpecies() {
        return this.species;
    }

    public void setSpecies(Species species) {
        this.species = species;
        this.setChanged();
        level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
        level.sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.save(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldPotState = potState;
        this.handleUpdateTag(this.getBlockState(), pkt.getTag());

        if (!oldPotState.equals(potState)) {
            ModelDataManager.requestModelDataRefresh(this);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    @Override
    public void load(BlockState state, CompoundTag tag) {
        if (tag.contains(POT_MIMIC_TAG)) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString(POT_MIMIC_TAG)));
            potState = block != Blocks.AIR ? block.defaultBlockState() : Blocks.FLOWER_POT.defaultBlockState();
        }
        if (tag.contains(SPECIES_TAG)) {
            this.species = TreeRegistry.findSpecies(tag.getString(SPECIES_TAG));
        }
        super.load(state, tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString(POT_MIMIC_TAG, potState.getBlock().getRegistryName().toString());
        tag.putString(SPECIES_TAG, this.species.getRegistryName().toString());
        return super.save(tag);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder().withInitial(POT_MIMIC, potState).withInitial(SPECIES, species).build();
    }

}
