package com.ferreusveritas.dynamictrees.systems.substance;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.waila.WailaOther;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level level, BlockPos rootPos) {

        BlockState blockState = level.getBlockState(rootPos);
        RootyBlock dirt = TreeHelper.getRooty(blockState);
        final Species species = dirt.getSpecies(blockState, level, rootPos);
        final Species megaSpecies = species.getMegaSpecies();

        if (megaSpecies.isValid()) {
            int fertility = dirt.getFertility(blockState, level, rootPos);
            megaSpecies.placeRootyDirtBlock(level, rootPos, fertility);

            blockState = level.getBlockState(rootPos);
            dirt = TreeHelper.getRooty(blockState);

            if (dirt.getSpecies(blockState, level, rootPos) == megaSpecies) {
                TreeHelper.treeParticles(level, rootPos, ParticleTypes.DRAGON_BREATH, 8);
                WailaOther.invalidateWailaPosition();
                return true;
            }
        }

        return false;
    }

    @Override
    public String getName() {
        return "mega";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
