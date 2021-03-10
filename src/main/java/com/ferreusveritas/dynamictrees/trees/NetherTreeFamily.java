package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicWartBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.MoundGenFeature;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.Optional;

public class NetherTreeFamily extends VanillaTreeFamily {

    public static class BaseNetherFungiSpecies extends Species{
        BaseNetherFungiSpecies(ResourceLocation name, Family family) {
            super(name, family);

            setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);

            envFactor(BiomeDictionary.Type.COLD, 0.25f);
            envFactor(BiomeDictionary.Type.WET, 0.75f);

            this.addGenFeature(GenFeatures.CLEAR_VOLUME);//Clear a spot for the thick tree trunk
            this.addGenFeature(GenFeatures.SHROOMLIGHT);
        }

        @Override
        protected void setStandardSoils() {
            addAcceptableSoils(DirtHelper.DIRT_LIKE, DirtHelper.NETHER_SOIL_LIKE, DirtHelper.FUNGUS_LIKE);
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
            return VoxelShapes.create(new AxisAlignedBB(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));
        }

        public DynamicLeavesBlock createLeavesBlock(LeavesProperties leavesProperties) {
            return (DynamicLeavesBlock) new DynamicWartBlock(leavesProperties).setRegistryName(getRegistryName() + "_wart");
        }
    }

    public class MegaNetherFungiSpecies extends BaseNetherFungiSpecies {
        MegaNetherFungiSpecies(ResourceLocation name, Family family) {
            super(name, family);

            setBasicGrowingParameters(0f, 25.0f, 7, 20, 0.9f);

            setSoilLongevity(16);//Grows for a while so it can actually get tall

            this.addGenFeature(GenFeatures.CONSISTENT_TRUNK);
            this.addGenFeature(GenFeatures.MOUND.with(MoundGenFeature.MOUND_CUTOFF_RADIUS, 999)); // Place a 3x3 of dirt under thick trees
        }

        @Override
        public ItemStack getSeedStack(int qty) {
            return getCommonSpecies().getSeedStack(qty);
        }

        @Override
        public Optional<Seed> getSeed() {
            return getCommonSpecies().getSeed();
        }

        @Override
        public int getMaxBranchRadius() {
            return 20;
        }

        @Override
        public boolean isThick() {
            return true;
        }

        @Override
        public boolean isMega() {
            return true;
        }

        @Override
        public boolean doesRequireTileEntity(IWorld world, BlockPos pos) {
            return true;
        }

    }

    public NetherTreeFamily(DynamicTrees.VanillaWoodTypes wood) {
        super(wood);
    }

    @Override
    public SoundType getBranchSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return SoundType.HYPHAE;
    }

    @Override
    public boolean isFireProof() { return true; }

    @Override
    public boolean isThick() {
        return true;
    }

}
