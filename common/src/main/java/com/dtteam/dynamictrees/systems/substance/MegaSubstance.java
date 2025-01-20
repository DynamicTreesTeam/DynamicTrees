package com.dtteam.dynamictrees.systems.substance;

import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.compat.WailaHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level level, BlockPos rootPos) {

        BlockState blockState = level.getBlockState(rootPos);
        SoilBlock dirt = TreeHelper.getRooty(blockState);
        final Species species = dirt.getSpecies(blockState, level, rootPos);
        final Species megaSpecies = species.getMegaSpecies();

        if (megaSpecies.isValid()) {
            int fertility = dirt.getFertility(blockState, level, rootPos);
            megaSpecies.placeRootyDirtBlock(level, rootPos, fertility);

            blockState = level.getBlockState(rootPos);
            dirt = TreeHelper.getRooty(blockState);

            if (dirt.getSpecies(blockState, level, rootPos) == megaSpecies) {
                TreeHelper.treeParticles(level, rootPos, ParticleTypes.DRAGON_BREATH, 8);
                WailaHelper.invalidateWailaPosition();
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
