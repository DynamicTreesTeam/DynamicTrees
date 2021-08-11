package com.ferreusveritas.dynamictrees.growthlogic.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class EnergyContext extends PositionalSpeciesContext {
    private final float signalEnergy;

    public EnergyContext(World world, BlockPos pos, Species species, float signalEnergy) {
        super(world, pos, species);
        this.signalEnergy = signalEnergy;
    }

    public float signalEnergy() {
        return signalEnergy;
    }
}
