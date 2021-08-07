package com.ferreusveritas.dynamictrees.trees.species;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilHelper;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreators;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class NetherFungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(NetherFungusSpecies::new);

    public NetherFungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        this.setSaplingShape(CommonVoxelShapes.SAPLING);
    }

    @Override
    public Species setDefaultGrowingParameters() {
        this.setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);
        return super.setDefaultGrowingParameters();
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(SoilHelper.NETHER_SOIL_LIKE, SoilHelper.FUNGUS_LIKE, SoilHelper.DIRT_LIKE);
    }

    @Override
    public Species setPreReloadDefaults() {
        return this.setDefaultGrowingParameters()
                .setSaplingShape(CommonVoxelShapes.FLAT_MUSHROOM)
                .setSaplingSound(SoundType.FUNGUS)
                .setCanSaplingGrowNaturally(false)
                .addDropCreators(DropCreators.LOG, DropCreators.WART_BLOCK)
                .envFactor(BiomeDictionary.Type.COLD, 0.25f)
                .envFactor(BiomeDictionary.Type.WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.hasGenFeatures()) {
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        }
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean isAcceptableSoilForWorldgen(IWorld world, BlockPos pos, BlockState soilBlockState) {
        if (soilBlockState.getBlock() == Blocks.NETHERRACK) {
            return true; //Soil exception for worldgen
        }
        return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
    }

    @Override
    public float defaultSeedComposterChance() {
        return 0.65f;
    }

    @Override
    public List<ITag.INamedTag<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<ITag.INamedTag<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

}
