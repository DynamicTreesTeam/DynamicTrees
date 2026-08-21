package com.dtteam.dynamictrees.systems.substance;

import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.compat.WailaHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaSubstance implements SubstanceEffect {

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
                TreeHelper.treeParticles(level, rootPos, ParticleTypes.HAPPY_VILLAGER, 8);
                WailaHelper.invalidateWailaPosition();
                return true;
            }
        }

        return false;
    }

    public String getName() {
        return "mega";
    }

    public boolean isLingering() {
        return false;
    }

}
