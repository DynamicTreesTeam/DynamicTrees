package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PalmGrowthLogic extends GrowthLogicKit {

    public PalmGrowthLogic(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {
        Direction originDir = signal.dir.getOpposite();

        // Alter probability map for direction change
        probMap[0] = 0; // Down is always disallowed for palm
        probMap[1] = 10;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] =  0;
        probMap[originDir.ordinal()] = 0; // Disable the direction we came from

        return probMap;
    }

    @Override
    public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
        return newDir;
    }

    @Override
    public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
        long day = world.getGameTime() / 24000L;
        int month = (int) day / 30; // Change the hashs every in-game month
        return signalEnergy * species.biomeSuitability(world, pos) + (CoordUtils.coordHashCode(pos.above(month), 3) % 3); // Vary the height energy by a psuedorandom hash function

    }

    @Override
    public int getLowestBranchHeight(World world, BlockPos pos, Species species, int lowestBranchHeight) {
        return lowestBranchHeight;
    }
}
