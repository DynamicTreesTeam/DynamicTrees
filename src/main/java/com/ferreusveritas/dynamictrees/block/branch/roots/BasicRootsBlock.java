package com.ferreusveritas.dynamictrees.block.branch.roots;

import com.ferreusveritas.dynamictrees.api.cell.Cell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicRootsBlock extends BranchBlock implements SimpleWaterloggedBlock {
    public static final String NAME_SUFFIX = "_roots";

    public static final IntegerProperty RADIUS = IntegerProperty.create("radius", 1, 8);
    public static final EnumProperty<Layer> LAYER = EnumProperty.create("layer", Layer.class);

    public enum Layer implements StringRepresentable {
        EXPOSED,
        FILLED,
        COVERED,
        WATERLOGGED;
        @Override public @NotNull String getSerializedName() {
            return toString().toLowerCase();
        }
    }

    public BasicRootsBlock(ResourceLocation name, Material material) {
        super(name, material);
    }

    @Override
    public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
        return null;
    }

    @Override
    public GrowSignal growSignal(Level level, BlockPos pos, GrowSignal signal) {
        return null;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from) {
        return 0;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return 0;
    }

    @Override
    public MapSignal analyse(BlockState state, LevelAccessor level, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        return null;
    }

    @Override
    public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public boolean checkForRot(LevelAccessor level, BlockPos pos, Species species, int fertility, int radius, RandomSource rand, float chance, boolean rapid) {
        return false;
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
        return 0;
    }

    @Override
    public BlockState getStateForRadius(int radius) {
        return null;
    }
}
