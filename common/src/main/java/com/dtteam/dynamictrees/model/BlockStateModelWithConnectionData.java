package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.Connections;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface BlockStateModelWithConnectionData {

    void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData);

}
