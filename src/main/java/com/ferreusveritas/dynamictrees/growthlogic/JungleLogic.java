package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class JungleLogic extends GrowthLogicKit {

    public JungleLogic(final ResourceLocation registryName) {
        super(registryName);
    }

    private static final int canopyHeight = 18;
    private static final int canopyHeightMega = 20;

    @Override
    public int[] directionManipulation(World world, BlockPos pos, Species species, int radius, GrowSignal signal, int[] probMap) {

        Direction originDir = signal.dir.getOpposite();

        int treeHash = CoordUtils.coordHashCode(signal.rootPos, 2);
        int posHash = CoordUtils.coordHashCode(pos, 2);

        //Alter probability map for direction change
        probMap[0] = 0;//Down is always disallowed for jungle
        probMap[1] = signal.isInTrunk() ? species.getUpProbability() : 0;
        probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
        int sideTurn = !signal.isInTrunk() || (signal.isInTrunk() && ((signal.numSteps + treeHash) % 5 == 0) && (radius > 1)) ? 2 : 0;//Only allow turns when we aren't in the trunk(or the branch is not a twig)

        int height = (species.isMegaSpecies() ? canopyHeightMega : canopyHeight) + ((treeHash % 7829) % 8);

        if (signal.delta.getY() < height) {
            probMap[2 + (posHash % 4)] = sideTurn;
        } else {
            probMap[1] = probMap[2] = probMap[3] = probMap[4] = probMap[5] = 2;//At top of tree allow any direction
        }

        probMap[originDir.ordinal()] = 0;//Disable the direction we came from
        probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction

        return probMap;
    }

    @Override
    public Direction newDirectionSelected(Species species, Direction newDir, GrowSignal signal) {
        if (signal.isInTrunk() && newDir != Direction.UP) {//Turned out of trunk
            signal.energy = 4.0f;
        }
        return newDir;
    }

    //Jungle trees grow taller in suitable biomes
    @Override
    public float getEnergy(World world, BlockPos pos, Species species, float signalEnergy) {
        return signalEnergy * species.biomeSuitability(world, pos);
    }

    @Override
    public int getLowestBranchHeight(World world, BlockPos pos, Species species, int lowestBranchHeight) {
        return lowestBranchHeight;
    }
}
