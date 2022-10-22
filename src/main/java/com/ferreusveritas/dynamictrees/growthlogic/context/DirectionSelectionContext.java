package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * @author Harley O'Connor
 */
public class DirectionSelectionContext extends PositionalSpeciesContext {

    private final BranchBlock branch;
    private final GrowSignal signal;

    public DirectionSelectionContext(Level level, BlockPos pos, Species species, BranchBlock branch, GrowSignal signal) {
        super(level, pos, species);
        this.branch = branch;
        this.signal = signal;
    }

    public BranchBlock branch() {
        return branch;
    }

    public GrowSignal signal() {
        return signal;
    }

}
