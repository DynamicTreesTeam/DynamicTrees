package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class GrowthSubstance implements SubstanceEffect {

    private final int pulses;
    private final int ticksPerPulse;
    private final int ticksPerParticlePulse = 8;
    private final boolean fillFertility;

    public GrowthSubstance() {
        this(-1, 24);
    }

    public GrowthSubstance(int pulses, int ticksPerPulse) {
        this(pulses, ticksPerPulse, true);
    }

    public GrowthSubstance(int pulses, int ticksPerPulse, boolean fillFertility) {
        this.pulses = pulses;
        this.ticksPerPulse = ticksPerPulse;
     	this.fillFertility = fillFertility;
    }

    @Override
    public boolean apply(Level world, BlockPos rootPos) {
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

    private int pulseCount;

    @Override
    public boolean update(Level level, BlockPos rootPos, int deltaTicks, int fertility) {
        // Stop when fertility has depleted.
        if (fertility <= 0 || this.pulseCount >= this.pulses) {
            return false;
        }

        if (level.isClientSide) {
            if (deltaTicks % this.ticksPerParticlePulse == 0) {
                TreeHelper.rootParticles(level, rootPos, Direction.UP, ParticleTypes.EFFECT, 1);
            }
        } else {
            if (deltaTicks % this.ticksPerPulse == 0) {
                TreeHelper.growPulse(level, rootPos);
                this.pulseCount++;
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
