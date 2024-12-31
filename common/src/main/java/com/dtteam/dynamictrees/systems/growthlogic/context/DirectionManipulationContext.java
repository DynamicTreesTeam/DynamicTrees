package com.dtteam.dynamictrees.systems.growthlogic.context;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public class DirectionManipulationContext extends DirectionSelectionContext {
    private final int radius;
    private int[] probMap;

    public DirectionManipulationContext(Level level, BlockPos pos, Species species,
                                        BranchBlock branch,
                                        GrowSignal signal, int radius, int[] probMap) {
        super(level, pos, species, branch, signal);
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
