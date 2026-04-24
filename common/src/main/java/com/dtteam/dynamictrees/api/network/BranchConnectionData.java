package com.dtteam.dynamictrees.api.network;

import net.minecraft.world.level.block.state.BlockState;

public record BranchConnectionData(BlockState blockState, Connections connections) { }
