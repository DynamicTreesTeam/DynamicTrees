package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

/**
 * @author Harley O'Connor
 */
public final class FungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(FungusSpecies::new);

    public FungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
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
        return super.setPreReloadDefaults().envFactor(BiomeDictionary.Type.COLD, 0.25f).envFactor(BiomeDictionary.Type.WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.areAnyGenFeatures())
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean canSaplingGrowNaturally(World world, BlockPos pos) {
        return false;
    }

    @Override
    public SoundType getSaplingSound() {
        return SoundType.FUNGUS;
    }

    @Override
    public VoxelShape getSaplingShape() {
        return VoxelShapes.box(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);
    }

}
