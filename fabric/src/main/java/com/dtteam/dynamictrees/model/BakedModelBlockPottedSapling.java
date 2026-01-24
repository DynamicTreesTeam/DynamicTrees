package com.dtteam.dynamictrees.model;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BakedModelBlockPottedSapling extends ForwardingBakedModel {

    public BakedModelBlockPottedSapling(BakedModel basePotModel) {
        this.wrapped = basePotModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return true;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(BlockState state, Direction face, RandomSource random) {
        return new ArrayList<>(wrapped.getQuads(state, face, random));
    }
}
