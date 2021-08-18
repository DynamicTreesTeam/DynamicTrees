package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GrowthLogicKit extends RegistryEntry<GrowthLogicKit> {

    public static final GrowthLogicKit NULL_LOGIC = new GrowthLogicKit(DTTrees.NULL) {
        @Override
        public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
            return probMap;
        }

        @Override
        public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
            return newDir;
        }

        @Override
        public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
            return signalEnergy;
        }

        @Override
        public int getLowestBranchHeight(World world, BlockPos pos, Species species, int lowestBranchHeight) {
            return lowestBranchHeight;
        }
    };

    /**
     * Central registry for all {@link GrowthLogicKit} objects.
     */
    public static final Registry<GrowthLogicKit> REGISTRY = new Registry<>(GrowthLogicKit.class, NULL_LOGIC);

    public GrowthLogicKit(final ResourceLocation registryName) {
        this.setRegistryName(registryName);
    }

    public Direction selectNewDirection(World world, BlockPos pos, Species species, BranchBlock branch, GrowSignal signal) {
        return null;
    }

    public abstract int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap);

    public Direction newDirectionSelected(World world, BlockPos pos, Species species, Direction newDir, GrowSignal signal){
        return this.newDirectionSelected(species, newDir, signal);
    }
    /**
     * @deprecated When possible override the version that includes {@link World} and {@link BlockPos}
     */
    @Deprecated
    public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) { return newDir; }

    public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) { return signalEnergy; };

    public int getLowestBranchHeight(World world, BlockPos pos, Species species, int lowestBranchHeight) { return lowestBranchHeight; };

}
