package com.ferreusveritas.dynamictrees.systems.substance;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substance.SubstanceEffect;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemapper.TransformNode;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TransformSubstance implements SubstanceEffect {

    private final Species toSpecies;

    public TransformSubstance(final Species toTree) {
        this.toSpecies = toTree;
    }

    @Override
    public boolean apply(Level level, BlockPos rootPos) {

        final BlockState rootyState = level.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootyState);

        if (dirt != null && this.toSpecies.isValid()) {
            Species fromSpecies = dirt.getSpecies(rootyState, level, rootPos);
            if (fromSpecies.isTransformable() && fromSpecies != this.toSpecies) {
                if (level.isClientSide) {
                    TreeHelper.treeParticles(level, rootPos, ParticleTypes.FIREWORK, 8);
                } else {
                    dirt.startAnalysis(level, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));
                }
                return true;
            }
        }


        return false;
    }

    @Override
    public String getName() {
        return "transform";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
