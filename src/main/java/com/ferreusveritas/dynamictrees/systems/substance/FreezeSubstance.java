package com.ferreusveritas.dynamictrees.systems.substance;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemapper.FreezerNode;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FreezeSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level level, BlockPos rootPos) {
        final BlockState rootyState = level.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootyState);
        final Species species = dirt.getSpecies(rootyState, level, rootPos);

        if (species != Species.NULL_SPECIES && dirt != null) {
            if (level.isClientSide) {
                TreeHelper.treeParticles(level, rootPos, ParticleTypes.FIREWORK, 8);
            } else {
                dirt.startAnalysis(level, rootPos, new MapSignal(new FreezerNode(species)));
                dirt.fertilize(level, rootPos, -15); // Destroy the fertility so it can no longer grow.
            }
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "freeze";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
