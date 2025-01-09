package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SoilBlockNF extends SoilBlock {

    public SoilBlockNF(SoilProperties properties, Properties blockProperties) {
        super(properties, blockProperties);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return getPrimitiveSoilBlock().getFireSpreadSpeed(getPrimitiveSoilState(state), level, pos, face);
    }

    @Override
    public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        return getPrimitiveSoilBlock().isFireSource(getPrimitiveSoilState(state), level, pos, side);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getPrimitiveSoilBlock().getFlammability(state, level, pos, direction);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getPrimitiveSoilBlock().getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getPrimitiveSoilBlock().getFriction(state, level, pos, entity);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return getPrimitiveSoilBlock().getSoundType(state, level, pos, entity);
    }
}
