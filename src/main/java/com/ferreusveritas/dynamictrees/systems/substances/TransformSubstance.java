package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemappers.TransformNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransformSubstance implements SubstanceEffect {

    private final Species toSpecies;

    public TransformSubstance(final Species toTree) {
        this.toSpecies = toTree;
    }

    @Override
    public boolean apply(World world, BlockPos rootPos) {

        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootyState);

        if (dirt != null && this.toSpecies.isValid()) {
            Species fromSpecies = dirt.getSpecies(rootyState, world, rootPos);
            if (fromSpecies.isTransformable() && fromSpecies != this.toSpecies) {
                if (world.isClientSide) {
                    TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
                } else {
                    dirt.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));
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
