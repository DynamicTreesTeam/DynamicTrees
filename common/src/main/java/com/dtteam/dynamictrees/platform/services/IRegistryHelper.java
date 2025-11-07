package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface IRegistryHelper {

    RegistryLoader getRegistryLoader();

    RegistryHandler newRegistryHandler();

    BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity();
    PottedSaplingBlockEntity newPottedSaplingBlockEntity(BlockPos pPos, BlockState pState);

}
