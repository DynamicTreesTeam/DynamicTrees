package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

/**
 * @author Harley O'Connor
 */
public class FungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(FungusSpecies::new);

    public FungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        this.setSaplingShape(DTRegistries.FLAT_MUSHROOM);
    }

    @Override
    public Species setDefaultGrowingParameters() {
        this.setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);
        return super.setDefaultGrowingParameters();
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(DirtHelper.NETHER_SOIL_LIKE, DirtHelper.FUNGUS_LIKE);
    }

    @Override
    public Species setPreReloadDefaults() {
        return super.setPreReloadDefaults().setSaplingSound(SoundType.FUNGUS).setCanSaplingGrowNaturally(false).setSaplingShape(DTRegistries.FLAT_MUSHROOM)
                .envFactor(BiomeDictionary.Type.COLD, 0.25f).envFactor(BiomeDictionary.Type.WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.areAnyGenFeatures())
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        return super.setPostReloadDefaults();
    }

}
