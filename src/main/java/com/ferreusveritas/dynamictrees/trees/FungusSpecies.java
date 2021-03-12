package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicWartBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

/**
 * @author Harley O'Connor
 */
public final class FungusSpecies extends Species {

    public FungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);

        this.setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);

        // Add default environment factors.
        this.defaultEnvFactor(BiomeDictionary.Type.COLD, 0.25f).defaultEnvFactor(BiomeDictionary.Type.WET, 0.75f);

        // Add default gen features.
        this.defaultGenFeature(GenFeatures.CLEAR_VOLUME)
                .defaultGenFeature(GenFeatures.SHROOMLIGHT);
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(DirtHelper.NETHER_SOIL_LIKE, DirtHelper.FUNGUS_LIKE);
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
        return VoxelShapes.create(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);
    }

    public static final class Type extends Species.Type {
        @Override
        public FungusSpecies construct(ResourceLocation registryName, Family family, LeavesProperties leavesProperties) {
            return new FungusSpecies(registryName, family, leavesProperties);
        }
    }

}
