package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicWartBlock;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.ClearVolumeGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.MoundGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.ShroomlightGenFeature;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Optional;

public class NetherTreeFamily extends VanillaTreeFamily {

    public static class BaseNetherFungiSpecies extends Species{
        BaseNetherFungiSpecies(ResourceLocation name, TreeFamily treeFamily) {
            super(name, treeFamily);

            envFactor(BiomeDictionary.Type.COLD, 0.25f);
            envFactor(BiomeDictionary.Type.WET, 0.75f);

            this.addGenFeature(GenFeatures.SHROOMLIGHT.getDefaultConfiguration());
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

        public DynamicLeavesBlock createLeavesBlock(ILeavesProperties leavesProperties) {
            return (DynamicLeavesBlock) new DynamicWartBlock(leavesProperties).setRegistryName(getRegistryName() + "_wart");
        }
    }

    public class MegaNetherFungiSpecies extends BaseNetherFungiSpecies {
        MegaNetherFungiSpecies(ResourceLocation name, TreeFamily treeFamily) {
            super(name, treeFamily);

            setSoilLongevity(16);//Grows for a while so it can actually get tall

            this.addGenFeature(GenFeatures.CLEAR_VOLUME);//Clear a spot for the thick tree trunk
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
        public int maxBranchRadius() {
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
        public boolean getRequiresTileEntity(IWorld world, BlockPos pos) {
            return true;
        }

    }

    public NetherTreeFamily(DynamicTrees.VanillaWoodTypes wood) {
        super(wood);
    }

    @Override
    public boolean isFireProof() {
        return true;
    }

    @Override
    public boolean isThick() {
        return true;
    }

}
