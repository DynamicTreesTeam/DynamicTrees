package com.dtteam.dynamictrees.systems.substance;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import com.dtteam.dynamictrees.systems.nodemapper.TransformNode;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.TreeHelper;
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
