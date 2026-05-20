package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
            mossyFamily.removeMossCarpet(state, level, pos, player);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        if (!isFullBlock(state) && getFamily() instanceof MossyAerialRootsFamily mossyFamily) {
            return mossyFamily.getMossCarpetBlock().defaultBlockState().getDestroySpeed(level, pos);
        }
        return super.getHardness(state, level, pos);
    }

    @Override
    protected BlockState coveredRootsState(BlockState state, Layer layer) {
        if (getFamily() instanceof MossyAerialRootsFamily mossyFamily){
            int rad = TreeHelper.getRadius(state);
            BlockState newState = mossyFamily.getRoots().get().getStateForRadius(rad);
            return newState.setValue(LAYER, layer).setValue(WATERLOGGED, false);
        }
        return super.coveredRootsState(state, layer);
    }

    @Override
    protected void washRoots(LevelAccessor level, BlockPos pos, BlockState state) {
        if (getFamily() instanceof MossyAerialRootsFamily mossyFamily){
            int rad = TreeHelper.getRadius(state);
            BlockState newState = mossyFamily.getRoots().get().getStateForRadius(rad);
            level.setBlock(pos, newState.setValue(BlockStateProperties.WATERLOGGED, true).setValue(LAYER, Layer.EXPOSED), 3);
        } else
            super.washRoots(level, pos, state);
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
        BlockState currentState = level.getBlockState(pos);
        boolean replacingGround = getAerialFamily().isAcceptableSoilForRootSystem(currentState);
        if (replacingGround && getFamily() instanceof AerialRootsFamily family && family.getRoots().isPresent()){
            return family.getRoots().get().setRadius(level, pos, radius, originDir, flags);
        } else {
            return super.setRadius(level, pos, radius, originDir, flags);
        }
    }
}
