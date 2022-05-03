package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class FertilizeSubstance implements SubstanceEffect {

    private int amount = 2;
    private boolean displayParticles = true;
    private boolean grow;
    private Supplier<Integer> pulses = () -> 1;

    @Override
    public boolean apply(Level world, BlockPos rootPos) {
        final RootyBlock dirt = TreeHelper.getRooty(world.getBlockState(rootPos));

        if (dirt != null && dirt.fertilize(world, rootPos, this.amount) || this.grow) {
            if (displayParticles && world.isClientSide) {
                TreeHelper.treeParticles(world, rootPos, ParticleTypes.HAPPY_VILLAGER, 8);
            } else {
                if (this.grow) {
                    final int pulses = this.pulses.get();

                    for (int i = 0; i < pulses; i++) {
                        TreeHelper.growPulse(world, rootPos);
                    }
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "fertilize";
    }

    public FertilizeSubstance setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * If growth is enabled then the tree will take an update and the item will be consumed.  Regardless of if it is
     * fully fertilised.
     *
     * @param grow
     * @return
     */
    public FertilizeSubstance setGrow(boolean grow) {
        this.grow = grow;
        return this;
    }

    public FertilizeSubstance setPulses(final int pulses) {
        return this.setPulses(() -> pulses);
    }

    public FertilizeSubstance setDisplayParticles(boolean displayParticles) {
        this.displayParticles = displayParticles;
        return this;
    }

    public FertilizeSubstance setPulses(final Supplier<Integer> pulses) {
        this.pulses = pulses;
        return this;
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
