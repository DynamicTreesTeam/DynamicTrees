package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class MossyRootsBlock extends BasicRootsBlock {

    public MossyRootsBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        SoundType sound = super.getSoundType(state);
        if (state.getValue(LAYER) != Layer.COVERED && getFamily() instanceof MossyAerialRootsFamily mossyFamily) {
            SoundType mossSound = mossyFamily.getMossCarpetSoundType();
            return new SoundType(sound.getVolume(), sound.getPitch(),
                    sound.getBreakSound(),
                    mossSound.getStepSound(),
                    sound.getPlaceSound(),
                    sound.getHitSound(),
                    mossSound.getFallSound());
        }
        return sound;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!isFullBlock(state) && getFamily() instanceof MossyAerialRootsFamily mossyFamily) {
            mossyFamily.removeMossCarpet(state, level, pos, player);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        if (!isFullBlock(state) && getFamily() instanceof MossyAerialRootsFamily mossyFamily) {
            return mossyFamily.getMossCarpetBlock().defaultBlockState().getDestroySpeed(level, pos);
        }
        return super.getHardness(state, level, pos);
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
        BlockState currentState = level.getBlockState(pos);
        UndergroundRootsFamily family = getFamily();
        boolean replacingGround = family != null && family.isAcceptableSoilForRootSystem(currentState);
        if (replacingGround && family.getRoots().isPresent()) {
            return family.getRoots().get().setRadius(level, pos, radius, originDir, flags);
        }
        return super.setRadius(level, pos, radius, originDir, flags);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (getFamily() instanceof MossyAerialRootsFamily mossyFamily && mossyFamily.getRoots().isPresent()) {
            int rad = TreeHelper.getTreePart(state).getRadius(state);
            BlockState newState = mossyFamily.getRoots().get().getStateForRadius(rad);
            return super.placeLiquid(level, pos, newState.setValue(BlockStateProperties.WATERLOGGED, false).setValue(LAYER, Layer.EXPOSED), fluidState);
        }
        return super.placeLiquid(level, pos, state, fluidState);
    }

}
