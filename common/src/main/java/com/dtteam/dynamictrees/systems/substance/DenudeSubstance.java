package com.dtteam.dynamictrees.systems.substance;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import com.dtteam.dynamictrees.systems.nodemapper.DenuderNode;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.TreeHelper;
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
    public boolean apply(Level level, BlockPos rootPos) {
        final BlockState rootState = level.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootState);

        if (dirt == null) {
            return false;
        }

        final Species species = dirt.getSpecies(rootState, level, rootPos);
        final Family family = species.getFamily();

        // If the family doesn't have a stripped branch the substance can't be applied.
        if (!family.hasStrippedBranch()) {
            return false;
        }

        // Set fertility to zero so the leaves won't grow back.
        dirt.setFertility(level, rootPos, 0);

        if (level.isClientSide) {
            TreeHelper.treeParticles(level, rootPos, ParticleTypes.ASH, 8);
        } else {
            dirt.startAnalysis(level, rootPos, new MapSignal(new DenuderNode(species, family)));
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
