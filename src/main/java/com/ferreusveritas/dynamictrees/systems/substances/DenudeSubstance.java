package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.nodemappers.DenuderNode;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An {@link SubstanceEffect} that "denudes" the tree. This involves stripping all branches and removing all leaves.
 *
 * @author Harley O'Connor
 */
public class DenudeSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level world, BlockPos rootPos) {
        final BlockState rootState = world.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootState);

        if (dirt == null) {
            return false;
        }

        final Species species = dirt.getSpecies(rootState, world, rootPos);
        final Family family = species.getFamily();

        // If the family doesn't have a stripped branch the substance can't be applied.
        if (!family.hasStrippedBranch()) {
            return false;
        }

        // Set fertility to zero so the leaves won't grow back.
        dirt.setFertility(world, rootPos, 0);

        if (world.isClientSide) {
            TreeHelper.treeParticles(world, rootPos, ParticleTypes.ASH, 8);
        } else {
            dirt.startAnalysis(world, rootPos, new MapSignal(new DenuderNode(species, family)));
        }

        return true;
    }

    @Override
    public String getName() {
        return "denude";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
