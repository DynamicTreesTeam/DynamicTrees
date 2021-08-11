package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class LowestBranchHeightContext extends PositionalSpeciesContext {
    private final int lowestBranchHeight;

    public LowestBranchHeightContext(World world, BlockPos pos, Species species, int lowestBranchHeight) {
        super(world, pos, species);
        this.lowestBranchHeight = lowestBranchHeight;
    }

    public int lowestBranchHeight() {
        return lowestBranchHeight;
    }
}
