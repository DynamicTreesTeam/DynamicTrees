package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlockEntity;
import com.dtteam.dynamictrees.registry.RegistryLoader;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public interface IRegistryHelper {

    RegistryLoader getRegistryLoader();

    RegistryHandler newRegistryHandler();
    RegistryHandler newRegistryHandler(String modId);

    BlockEntityType.BlockEntitySupplier<PottedSaplingBlockEntity> getPottedSaplingBlockEntity();

}
