package com.ferreusveritas.dynamictrees.systems.substance;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class DepleteSubstance implements SubstanceEffect {

    int amount;

    @Override
    public boolean apply(Level level, BlockPos rootPos) {
        final RootyBlock dirt = TreeHelper.getRooty(level.getBlockState(rootPos));

        if (dirt.fertilize(level, rootPos, -amount)) {
            TreeHelper.treeParticles(level, rootPos, ParticleTypes.ANGRY_VILLAGER, 8);
            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "deplete";
    }

    public DepleteSubstance setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
