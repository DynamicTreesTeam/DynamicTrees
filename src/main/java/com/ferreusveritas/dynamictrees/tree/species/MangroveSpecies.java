package com.ferreusveritas.dynamictrees.tree.species;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.growthlogic.context.PositionalSpeciesContext;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.family.MangroveFamily;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.security.InvalidParameterException;

public class MangroveSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(MangroveSpecies::new);

    private int minWorldGenHeightOffset = 3;
    private int maxWorldGenHeightOffset = 8;

    public void setMinWorldGenHeightOffset(int minWorldGenHeightOffset) {
        this.minWorldGenHeightOffset = minWorldGenHeightOffset;
    }

    public void setMaxWorldGenHeightOffset(int maxWorldGenHeightOffset) {
        this.maxWorldGenHeightOffset = maxWorldGenHeightOffset;
    }

    public MangroveSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        if (!(family instanceof MangroveFamily)) {
            throw new RuntimeException("Family " + family.toString() + " for mangrove species " + name + "is not of type "+ MangroveFamily.class);
        }
    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return DynamicTrees.location("block/smartmodel/water_sapling_thin");
    }

    public MangroveFamily getFamily() {
        return (MangroveFamily) family;
    }

    //////////////////////
    // ROOTY SOIL
    //////////////////////

    public boolean placeRootyDirtBlock(LevelAccessor level, BlockPos rootPos, int fertility) {
        BlockState dirtState = level.getBlockState(rootPos);
        Block dirt = dirtState.getBlock();

        if (!SoilHelper.isSoilRegistered(dirt) && !(dirt instanceof RootyBlock)) {
            //soil is not valid so we place default roots
            level.setBlock(rootPos, getFamily().getDefaultSoil().getSoilState(dirtState, fertility, this.doesRequireTileEntity(level, rootPos)), 3);

            BlockEntity tileEntity = level.getBlockEntity(rootPos);
            if (tileEntity instanceof SpeciesBlockEntity speciesTE) {
                speciesTE.setSpecies(this);
            }
            return true;
        }

        return super.placeRootyDirtBlock(level, rootPos, fertility);
    }

    @Override
    public boolean postGrow(Level level, BlockPos rootPos, BlockPos treePos, int fertility, boolean natural) {
        int radius = TreeHelper.getRadius(level, treePos);
        if (radius >= 8) {
            BlockState soilState = level.getBlockState(rootPos);
            if (soilState.getBlock() instanceof RootyBlock rootyBlock
                    && !rootyBlock.getSoilProperties().equals(getFamily().getDefaultSoil())){
                BlockEntity TE = level.getBlockEntity(treePos);
                level.setBlock(rootPos, getFamily().getDefaultSoil().getSoilState(rootyBlock.getPrimitiveSoilState(soilState), fertility, soilState.getValue(RootyBlock.IS_VARIANT)), 3);
                if (TE != null){
                    level.setBlockEntity(TE);
                    if (TE instanceof SpeciesBlockEntity speciesTE) {
                        speciesTE.setSpecies(this);
                    }
                }

            }
        }
        return super.postGrow(level, rootPos, treePos, fertility, natural);
    }

    //////////////////////
    // GENERATION
    //////////////////////


    @Override
    public boolean generate(GenerationContext context) {
        context.rootPos().move(Direction.UP,
                context.random().nextIntBetweenInclusive(minWorldGenHeightOffset, maxWorldGenHeightOffset));
        return super.generate(context);
    }


    //////////////////////
    // GROWTH
    //////////////////////

    @Override
    protected GrowSignal sendGrowthSignal(TreePart treeBase, Level level, BlockPos treePos, BlockPos rootPos) {
        GrowSignal treeSignal = super.sendGrowthSignal(treeBase, level, treePos, rootPos);

        if (treeSignal.success){
            BlockPos belowPos = rootPos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (TreeHelper.isBranch(belowState)){
                GrowSignal rootGrowSignal = new GrowSignal(this, rootPos, getRootEnergy(level, rootPos), level.random);
                return TreeHelper.getTreePart(belowState).growSignal(level, treePos, rootGrowSignal);
            } else {
                getFamily().getRoot().ifPresent(branch -> branch.setRadius(level, belowPos, family.getPrimaryThickness(), null));
            }
        }

        return treeSignal;
    }

    public float getRootEnergy(Level level, BlockPos rootPos) {
        return this.logicKit.getEnergy(new PositionalSpeciesContext(level, rootPos, this)) / 2f;
    }
}
