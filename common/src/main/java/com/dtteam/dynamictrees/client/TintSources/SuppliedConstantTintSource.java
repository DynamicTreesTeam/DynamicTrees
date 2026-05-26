package com.dtteam.dynamictrees.client.TintSources;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class SuppliedConstantTintSource implements BlockTintSource {

    Supplier<Integer> color;

    public SuppliedConstantTintSource(Supplier<Integer> color){
        this.color = color;
    }

    @Override
    public int color(BlockState blockState) {
        return color.get();
    }
}