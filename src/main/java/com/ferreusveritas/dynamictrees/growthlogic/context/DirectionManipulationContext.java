package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class DirectionManipulationContext extends DirectionSelectionContext {
    private final int radius;
    private int[] probMap;

    public DirectionManipulationContext(World world, BlockPos pos, Species species,
                                        BranchBlock branch,
                                        GrowSignal signal, int radius, int[] probMap) {
        super(world, pos, species, branch, signal);
        this.radius = radius;
        this.probMap = probMap;
    }

    public int radius() {
        return radius;
    }

    public int[] probMap() {
        return probMap;
    }

    public void probMap(int[] probMap) {
        this.probMap = probMap;
    }
}
