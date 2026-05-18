package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MossyRootsBlock extends BasicRootsBlock {

    public MossyRootsBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        SoundType sound = super.getSoundType(state);
        if (state.getValue(LAYER) != Layer.COVERED && getFamily() instanceof MossyAerialRootsFamily mossyFamily){
            SoundType mossSound = mossyFamily.getMossCarpetSoundType();

            return new SoundType(sound.volume, sound.pitch,
                    sound.getBreakSound(),
                    mossSound.getStepSound(),
                    sound.getPlaceSound(),
                    sound.getHitSound(),
                    mossSound.getFallSound());
        }
        return sound;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if (!isFullBlock(state) && getFamily() instanceof MossyAerialRootsFamily mossyFamily){
            mossyFamily.removeMossCarpet(state, level, pos);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    @Override
    protected BlockState coveredRootsState(BlockState state, Layer layer) {
        if (getFamily() instanceof MossyAerialRootsFamily mossyFamily){
            int rad = TreeHelper.getRadius(state);
            BlockState newState = mossyFamily.getMossyRoots().get().getStateForRadius(rad);
            return newState.setValue(LAYER, layer).setValue(WATERLOGGED, false);
        }
        return super.coveredRootsState(state, layer);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        if (!isFullBlock(state) && getFamily() instanceof MossyAerialRootsFamily mossyFamily) {
            return mossyFamily.getMossCarpetBlock().defaultBlockState().getDestroySpeed(level, pos);
        }
        return super.getHardness(state, level, pos);
    }
}
