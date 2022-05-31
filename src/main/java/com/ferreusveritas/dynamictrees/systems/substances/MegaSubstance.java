package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.substances.SubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.compat.waila.WailaOther;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MegaSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level world, BlockPos rootPos) {

        BlockState blockState = world.getBlockState(rootPos);
        RootyBlock dirt = TreeHelper.getRooty(blockState);
        final Species species = dirt.getSpecies(blockState, world, rootPos);
        final Species megaSpecies = species.getMegaSpecies();

        if (megaSpecies.isValid()) {
            int fertility = dirt.getFertility(blockState, world, rootPos);
            megaSpecies.placeRootyDirtBlock(world, rootPos, fertility);

            blockState = world.getBlockState(rootPos);
            dirt = TreeHelper.getRooty(blockState);

            if (dirt.getSpecies(blockState, world, rootPos) == megaSpecies) {
                TreeHelper.treeParticles(world, rootPos, ParticleTypes.DRAGON_BREATH, 8);
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
