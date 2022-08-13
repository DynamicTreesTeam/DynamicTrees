package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthSubstance implements SubstanceEffect {

    private final int ticksPerPulse;
    private final int ticksPerParticlePulse;
    private final boolean fillFertility;

    public GrowthSubstance() {
        this(24, 8, true);
    }

    public GrowthSubstance(int ticksPerPulse, int ticksPerParticlePulse, boolean fillFertility) {
        this.ticksPerPulse = ticksPerPulse;
        this.ticksPerParticlePulse = ticksPerParticlePulse;
        this.fillFertility = fillFertility;
    }

    @Override
    public boolean apply(World world, BlockPos rootPos) {
        // Don't apply if there is already a growth substance.
        if (LingeringEffectorEntity.treeHasEffectorForEffect(world, rootPos, this)) {
            return false;
        }
		if (fillFertility) {
        	new FertilizeSubstance().setAmount(15).setDisplayParticles(false).apply(world, rootPos);
		}

        TreeHelper.treeParticles(world, rootPos, ParticleTypes.EFFECT, 8);
        return true;
    }

    @Override
    public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
        // Stop when fertility has depleted.
        if (fertility <= 0) {
            return false;
        }

        if (world.isClientSide) {
            if (deltaTicks % this.ticksPerParticlePulse == 0) {
                TreeHelper.rootParticles(world, rootPos, Direction.UP, ParticleTypes.EFFECT, 1);
            }
        } else {
            if (deltaTicks % this.ticksPerPulse == 0) {
                TreeHelper.growPulse(world, rootPos);
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "growth";
    }

    @Override
    public boolean isLingering() {
        return true;
    }

}
